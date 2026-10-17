package id.maetaruna.assessment.userapi.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import id.maetaruna.assessment.userapi.model.User;
import java.util.List;

public record UserResponse(@JsonUnwrapped User user, List<TodoItem> todos, List<PostItem> posts) {
}
