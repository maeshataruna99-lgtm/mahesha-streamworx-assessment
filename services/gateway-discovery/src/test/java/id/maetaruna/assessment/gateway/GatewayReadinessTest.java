package id.maetaruna.assessment.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.cloud.discovery.client.simple.instances.api-2-post-xml[0].uri=http://localhost:9")
class GatewayReadinessTest {

    @LocalServerPort
    int port;

    @Test
    void isOutOfServiceWhileARoutedServiceIsNotRegistered() throws Exception {
        HttpResponse<String> health;
        try (HttpClient client = HttpClient.newHttpClient()) {
            health = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/actuator/health")).build(),
                    HttpResponse.BodyHandlers.ofString());
        }

        assertThat(health.statusCode()).isEqualTo(503);
        assertThat(health.body()).contains("OUT_OF_SERVICE");
    }
}
