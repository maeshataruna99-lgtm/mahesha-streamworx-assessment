package id.maetaruna.assessment.postapi.controller;

import id.maetaruna.assessment.postapi.dto.PostRow;
import id.maetaruna.assessment.postapi.dto.PostsXml;
import id.maetaruna.assessment.postapi.repository.PostRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostController {

    private final PostRepository postRepository;

    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping(value = "/api/posts", produces = MediaType.APPLICATION_XML_VALUE)
    public PostsXml getPosts() {
        return new PostsXml(postRepository.findAllByOrderByIdAsc().stream()
                .map(PostRow::from)
                .toList());
    }
}
