package id.maetaruna.assessment.postapi.repository;

import id.maetaruna.assessment.postapi.entity.Post;
import java.util.List;
import org.springframework.data.repository.Repository;

public interface PostRepository extends Repository<Post, Integer> {

    List<Post> findAllByOrderByIdAsc();
}
