package id.maetaruna.assessment.userapi.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import id.maetaruna.assessment.userapi.config.ClientProperties.Endpoint;
import id.maetaruna.assessment.userapi.config.RestClientConfig;
import id.maetaruna.assessment.userapi.dto.PostItem;
import id.maetaruna.assessment.userapi.support.StubHttpServer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PostClientTest {

    private static final String XML = """
            <?xml version='1.0' encoding='UTF-8'?><root>\
            <row><userId>1</userId><id>1</id><title>first</title><body>line one
            line two</body></row>\
            <row><userId>2</userId><id>11</id><title>eleventh</title><body>other user</body></row>\
            <row><userId>1</userId><id>2</id><title>second</title><body>b</body></row>\
            </root>""";

    @Test
    void groupsPostsByUserKeepingApiOrder() {
        try (StubHttpServer api2 = StubHttpServer.respond("/api/posts", 200, "application/xml", XML)) {
            Map<Integer, List<PostItem>> posts = client(api2).postsByUserId();

            assertThat(posts).containsOnlyKeys(1, 2);
            assertThat(posts.get(1)).containsExactly(
                    new PostItem(1, "first", "line one\nline two"),
                    new PostItem(2, "second", "b"));
            assertThat(posts.get(2)).containsExactly(new PostItem(11, "eleventh", "other user"));
        }
    }

    @Test
    void returnsNoPostsForAnEmptyRoot() {
        try (StubHttpServer api2 = StubHttpServer.respond("/api/posts", 200, "application/xml",
                "<?xml version='1.0' encoding='UTF-8'?><root/>")) {
            assertThat(client(api2).postsByUserId()).isEmpty();
        }
    }

    @Test
    void reportsApi2FailureInsteadOfReturningPartialData() {
        try (StubHttpServer api2 = StubHttpServer.respond("/api/posts", 500, "text/plain", "boom")) {
            assertThatThrownBy(() -> client(api2).postsByUserId()).isInstanceOf(PostsUnavailableException.class);
        }
    }

    private static PostClient client(StubHttpServer server) {
        return new PostClient(RestClientConfig.restClient(
                new Endpoint(server.baseUrl(), Duration.ofSeconds(1), Duration.ofSeconds(2))));
    }
}
