package id.maetaruna.assessment.userapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "eureka.client.enabled=false",
        "clients.todos.base-url=http://localhost:9"})
@Import(RedisContainerConfig.class)
class UsersWithoutApi2Test {

    @LocalServerPort
    int port;

    @Test
    void answersServiceUnavailableWhileApi2HasNoRegisteredInstance() throws Exception {
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/users")).build(),
                    HttpResponse.BodyHandlers.ofString());
        }

        assertThat(response.statusCode()).isEqualTo(503);
        assertThat(response.body()).contains("Posts service is unavailable");
    }
}
