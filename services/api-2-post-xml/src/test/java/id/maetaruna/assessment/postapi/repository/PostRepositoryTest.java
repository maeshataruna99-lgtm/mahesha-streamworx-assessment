package id.maetaruna.assessment.postapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import id.maetaruna.assessment.postapi.PostgresContainerConfig;
import id.maetaruna.assessment.postapi.entity.Post;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresContainerConfig.class)
class PostRepositoryTest {

    @Autowired
    PostRepository postRepository;

    @Test
    void readsAllSeededPostsOrderedById() {
        List<Post> posts = postRepository.findAllByOrderByIdAsc();

        assertThat(posts).hasSize(100);

        Post first = posts.getFirst();
        assertThat(first.getId()).isEqualTo(1);
        assertThat(first.getUserId()).isEqualTo(1);
        assertThat(first.getTitle())
                .isEqualTo("sunt aut facere repellat provident occaecati excepturi optio reprehenderit");
        assertThat(first.getBody()).startsWith("quia et suscipit\nsuscipit recusandae");

        Post last = posts.getLast();
        assertThat(last.getId()).isEqualTo(100);
        assertThat(last.getUserId()).isEqualTo(10);
    }
}
