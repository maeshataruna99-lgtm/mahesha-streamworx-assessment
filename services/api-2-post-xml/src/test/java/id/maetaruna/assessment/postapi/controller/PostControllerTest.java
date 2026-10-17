package id.maetaruna.assessment.postapi.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import id.maetaruna.assessment.postapi.entity.Post;
import id.maetaruna.assessment.postapi.repository.PostRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PostRepository postRepository;

    @Test
    void returnsPostsAsRowsUnderRoot() throws Exception {
        given(postRepository.findAllByOrderByIdAsc()).willReturn(List.of(
                new Post(1, 1, "first title", "line one\nline two"),
                new Post(11, 2, "second title", "second body")));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(xpath("count(/root/row)").number(2.0))
                .andExpect(xpath("name(/root/row[1]/*[1])").string("userId"))
                .andExpect(xpath("name(/root/row[1]/*[2])").string("id"))
                .andExpect(xpath("name(/root/row[1]/*[3])").string("title"))
                .andExpect(xpath("name(/root/row[1]/*[4])").string("body"))
                .andExpect(xpath("/root/row[1]/body").string("line one\nline two"))
                .andExpect(xpath("/root/row[2]/userId").string("2"))
                .andExpect(xpath("/root/row[2]/id").string("11"))
                .andExpect(xpath("/root/row[2]/title").string("second title"));
    }

    @Test
    void returnsEmptyRootWhenThereAreNoPosts() throws Exception {
        given(postRepository.findAllByOrderByIdAsc()).willReturn(List.of());

        mockMvc.perform(get("/api/posts").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(xpath("/root").exists())
                .andExpect(xpath("count(/root/row)").number(0.0));
    }
}
