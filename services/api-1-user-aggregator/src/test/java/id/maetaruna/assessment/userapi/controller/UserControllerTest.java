package id.maetaruna.assessment.userapi.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import id.maetaruna.assessment.userapi.client.PostsUnavailableException;
import id.maetaruna.assessment.userapi.dto.PostItem;
import id.maetaruna.assessment.userapi.dto.TodoItem;
import id.maetaruna.assessment.userapi.dto.UserResponse;
import id.maetaruna.assessment.userapi.model.Address;
import id.maetaruna.assessment.userapi.model.Company;
import id.maetaruna.assessment.userapi.model.Geo;
import id.maetaruna.assessment.userapi.model.User;
import id.maetaruna.assessment.userapi.service.UserAggregationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserAggregationService service;

    @Test
    void returnsUsersAsJsonWithTodosAndPostsInline() throws Exception {
        User leanne = new User(1, "Leanne Graham", "Bret", "Sincere@april.biz",
                new Address("Kulas Light", "Apt. 556", "Gwenborough", "92998-3874", new Geo("-37.3159", "81.1496")),
                "1-770-736-8031 x56442", "hildegard.org",
                new Company("Romaguera-Crona", "Multi-layered client-server neural-net", "harness real-time e-markets"));
        given(service.getUsers()).willReturn(List.of(new UserResponse(leanne,
                List.of(new TodoItem(1, "delectus aut autem", false)),
                List.of(new PostItem(1, "sunt aut facere", "quia et suscipit")))));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Leanne Graham"))
                .andExpect(jsonPath("$[0].user").doesNotExist())
                .andExpect(jsonPath("$[0].address.geo.lat").value("-37.3159"))
                .andExpect(jsonPath("$[0].company.bs").value("harness real-time e-markets"))
                .andExpect(jsonPath("$[0].todos[0].title").value("delectus aut autem"))
                .andExpect(jsonPath("$[0].todos[0].completed").value(false))
                .andExpect(jsonPath("$[0].todos[0].userId").doesNotExist())
                .andExpect(jsonPath("$[0].posts[0].body").value("quia et suscipit"));
    }

    @Test
    void staysJsonWhenTheClientPrefersXml() throws Exception {
        given(service.getUsers()).willReturn(List.of());

        mockMvc.perform(get("/api/users")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void answersServiceUnavailableWhenPostsCannotBeLoaded() throws Exception {
        given(service.getUsers()).willThrow(new PostsUnavailableException(new RuntimeException("connection refused")));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Posts service is unavailable"));
    }
}
