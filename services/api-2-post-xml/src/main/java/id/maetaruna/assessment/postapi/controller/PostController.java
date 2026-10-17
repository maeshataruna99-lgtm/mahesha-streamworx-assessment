package id.maetaruna.assessment.postapi.controller;

import id.maetaruna.assessment.postapi.dto.PostRow;
import id.maetaruna.assessment.postapi.dto.PostsXml;
import id.maetaruna.assessment.postapi.repository.PostRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.dataformat.xml.XmlMapper;

@RestController
public class PostController {

    // Jackson's XML writer always emits the declaration with single quotes, so it is written here
    // to match the assignment's sample exactly. The body is indented like the sample as well.
    private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    private final PostRepository postRepository;
    private final XmlMapper xmlMapper;

    public PostController(PostRepository postRepository, XmlMapper xmlMapper) {
        this.postRepository = postRepository;
        this.xmlMapper = xmlMapper;
    }

    @GetMapping(value = "/api/posts", produces = MediaType.APPLICATION_XML_VALUE)
    public String getPosts() {
        PostsXml posts = new PostsXml(postRepository.findAllByOrderByIdAsc().stream()
                .map(PostRow::from)
                .toList());
        return XML_DECLARATION + "\n" + xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(posts);
    }
}
