package id.maetaruna.assessment.userapi.client;

import static org.assertj.core.api.Assertions.assertThat;

import id.maetaruna.assessment.userapi.config.ClientProperties.Endpoint;
import id.maetaruna.assessment.userapi.config.RestClientConfig;
import id.maetaruna.assessment.userapi.dto.TodoItem;
import id.maetaruna.assessment.userapi.support.StubHttpServer;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TodoClientTest {

    private static final String TODOS = """
            [
              {"userId": 1, "id": 1, "title": "delectus aut autem", "completed": false},
              {"userId": 2, "id": 21, "title": "suscipit repellat esse quibusdam voluptatem incidunt", "completed": false},
              {"userId": 1, "id": 4, "title": "et porro tempora", "completed": true}
            ]""";

    private static final Duration READ_TIMEOUT = Duration.ofMillis(500);

    @Test
    void groupsTodosByUser() {
        try (StubHttpServer todos = StubHttpServer.respond("/todos", 200, "application/json", TODOS)) {
            Map<Integer, List<TodoItem>> byUser = client(todos.baseUrl()).todosByUserId();

            assertThat(byUser.get(1)).containsExactly(
                    new TodoItem(1, "delectus aut autem", false),
                    new TodoItem(4, "et porro tempora", true));
            assertThat(byUser.get(2)).containsExactly(
                    new TodoItem(21, "suscipit repellat esse quibusdam voluptatem incidunt", false));
        }
    }

    @Test
    void fallsBackToNoTodosWhenTheServiceIsSlow() {
        try (StubHttpServer todos = StubHttpServer.respond("/todos", 200, "application/json", TODOS,
                Duration.ofSeconds(3))) {
            long start = System.nanoTime();

            Map<Integer, List<TodoItem>> byUser = client(todos.baseUrl()).todosByUserId();

            assertThat(byUser).isEmpty();
            assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofMillis(1500));
        }
    }

    @Test
    void fallsBackToNoTodosOnServerError() {
        try (StubHttpServer todos = StubHttpServer.respond("/todos", 503, "text/plain", "down")) {
            assertThat(client(todos.baseUrl()).todosByUserId()).isEmpty();
        }
    }

    @Test
    void fallsBackToNoTodosWhenUnreachable() {
        URI closedPort;
        try (StubHttpServer stopped = StubHttpServer.respond("/todos", 200, "application/json", TODOS)) {
            closedPort = stopped.baseUrl();
        }
        assertThat(client(closedPort).todosByUserId()).isEmpty();
    }

    private static TodoClient client(URI baseUrl) {
        return new TodoClient(RestClientConfig.restClient(new Endpoint(baseUrl, Duration.ofMillis(500), READ_TIMEOUT)));
    }
}
