package id.maetaruna.assessment.userapi;

import static org.assertj.core.api.Assertions.assertThat;

import id.maetaruna.assessment.userapi.support.StubHttpServer;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(RedisContainerConfig.class)
class UsersJsonContractTest {

    private static final StubHttpServer API_2 = StubHttpServer.respond(
            "/api/posts", 200, "application/xml", fixture("contract/api-2-posts.xml"));
    private static final StubHttpServer TODOS = StubHttpServer.respond(
            "/todos", 200, "application/json", fixture("contract/todos.json"));

    @LocalServerPort
    int port;

    @DynamicPropertySource
    static void clients(DynamicPropertyRegistry registry) {
        registry.add("clients.posts.base-url", () -> API_2.baseUrl().toString());
        registry.add("clients.todos.base-url", () -> TODOS.baseUrl().toString());
    }

    @AfterAll
    static void stopStubs() {
        API_2.close();
        TODOS.close();
    }

    @Test
    void firstUserMatchesTheSampleFromTheAssignment() throws Exception {
        String actual = getUsers();
        String sample = fixture("contract/api-1-sample.json");
        JsonNode users = JsonMapper.shared().readTree(actual);

        assertThat(users).hasSize(10);
        assertThat(users.get(0).propertyNames()).containsExactly(
                "id", "name", "username", "email", "address", "phone", "website", "company", "todos", "posts");

        // The sample in the assignment abbreviates post bodies, so they are not compared.
        String firstUser = "[" + users.get(0) + "]";
        JSONAssert.assertEquals(sample, firstUser, new CustomComparator(JSONCompareMode.STRICT,
                new Customization("[*].posts[*].body", (expected, actualBody) -> true)));
        assertThat(users.get(0).at("/posts/0/body").asString()).startsWith("quia et suscipit\nsuscipit recusandae");
    }

    @Test
    void usersWithoutUpstreamDataGetEmptyLists() throws Exception {
        JsonNode lastUser = JsonMapper.shared().readTree(getUsers()).get(9);

        assertThat(lastUser.get("name").asString()).isEqualTo("Clementina DuBuque");
        assertThat(lastUser.get("todos").isEmpty()).isTrue();
        assertThat(lastUser.get("posts").isEmpty()).isTrue();
    }

    private String getUsers() throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/users")).build(),
                    HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(200);
            return response.body();
        }
    }

    private static String fixture(String path) {
        try {
            return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
