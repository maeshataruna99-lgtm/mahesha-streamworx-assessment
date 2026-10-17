package id.maetaruna.assessment.userapi.client;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import id.maetaruna.assessment.userapi.dto.PostItem;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class PostClient {

    private final RestClient restClient;

    public PostClient(@Qualifier("postRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public Map<Integer, List<PostItem>> postsByUserId() {
        PostsXml posts;
        try {
            posts = restClient.get()
                    .uri("/api/posts")
                    .accept(MediaType.APPLICATION_XML)
                    .retrieve()
                    .body(PostsXml.class);
        } catch (RestClientException | IllegalStateException e) {
            // The load balancer throws IllegalStateException when api-2 has no registered instance.
            throw new PostsUnavailableException(e);
        }
        if (posts == null || posts.rows() == null) {
            return Map.of();
        }
        return posts.rows().stream().collect(groupingBy(
                PostsXml.Row::userId,
                mapping(row -> new PostItem(row.id(), row.title(), row.body()), toList())));
    }
}
