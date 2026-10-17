package id.maetaruna.assessment.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutesTest {

    private static final HttpServer API_1 = stub("/api/users", "application/json", "[{\"id\":1}]");
    private static final HttpServer API_2 = stub("/api/posts", "application/xml", "<root><row><id>1</id></row></root>");

    @LocalServerPort
    int port;

    // The services are resolved by name through the discovery client, as Eureka would in compose.
    @DynamicPropertySource
    static void instances(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.discovery.client.simple.instances.api-1-user-aggregator[0].uri",
                () -> "http://localhost:" + API_1.getAddress().getPort());
        registry.add("spring.cloud.discovery.client.simple.instances.api-2-post-xml[0].uri",
                () -> "http://localhost:" + API_2.getAddress().getPort());
    }

    @AfterAll
    static void stopStubs() {
        API_1.stop(0);
        API_2.stop(0);
    }

    @Test
    void routesUsersToApi1() throws Exception {
        HttpResponse<String> response = get("/api/users");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("[{\"id\":1}]");
        assertThat(response.headers().firstValue("Content-Type")).hasValue("application/json");
    }

    @Test
    void routesPostsToApi2() throws Exception {
        HttpResponse<String> response = get("/api/posts");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("<root><row><id>1</id></row></root>");
    }

    @Test
    void doesNotRouteUnknownPaths() throws Exception {
        assertThat(get("/api/unknown").statusCode()).isEqualTo(404);
    }

    @Test
    void isHealthyOnceEveryRoutedServiceIsRegistered() throws Exception {
        HttpResponse<String> response = get("/actuator/health");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"UP\"");
    }

    @Test
    void servesTheEurekaRegistry() throws Exception {
        HttpResponse<String> response = get("/eureka/apps");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("<applications>");
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).build(),
                    HttpResponse.BodyHandlers.ofString());
        }
    }

    private static HttpServer stub(String path, String contentType, String body) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext(path, exchange -> {
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(bytes);
                }
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
