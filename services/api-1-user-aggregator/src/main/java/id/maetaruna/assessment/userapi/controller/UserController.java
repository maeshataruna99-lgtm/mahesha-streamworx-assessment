package id.maetaruna.assessment.userapi.controller;

import id.maetaruna.assessment.userapi.client.PostsUnavailableException;
import id.maetaruna.assessment.userapi.dto.UserResponse;
import id.maetaruna.assessment.userapi.service.UserAggregationService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserAggregationService userAggregationService;

    public UserController(UserAggregationService userAggregationService) {
        this.userAggregationService = userAggregationService;
    }

    // produces is explicit because jackson-dataformat-xml is on the classpath (to read api-2),
    // and without it a browser, whose Accept header prefers XML, would get XML back.
    @GetMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserResponse> getUsers() {
        return userAggregationService.getUsers();
    }

    @ExceptionHandler(PostsUnavailableException.class)
    ProblemDetail postsUnavailable(PostsUnavailableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }
}
