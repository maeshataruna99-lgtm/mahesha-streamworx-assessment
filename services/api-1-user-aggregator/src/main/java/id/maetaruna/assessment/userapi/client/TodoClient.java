package id.maetaruna.assessment.userapi.client;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import id.maetaruna.assessment.userapi.dto.TodoItem;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class TodoClient {

    private static final Logger log = LoggerFactory.getLogger(TodoClient.class);

    private final RestClient restClient;

    public TodoClient(@Qualifier("todoRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    // Todos come from a public service outside our control, so users are still returned
    // (with empty todos) when it is slow or down. All todos are fetched in one call and
    // grouped here instead of calling /todos?userId= once per user.
    public Map<Integer, List<TodoItem>> todosByUserId() {
        List<Todo> todos;
        try {
            todos = restClient.get()
                    .uri("/todos")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (RestClientException e) {
            log.warn("Todos service unavailable, returning users without todos: {}", e.getMessage());
            return Map.of();
        }
        if (todos == null) {
            return Map.of();
        }
        return todos.stream().collect(groupingBy(
                Todo::userId,
                mapping(todo -> new TodoItem(todo.id(), todo.title(), todo.completed()), toList())));
    }

    record Todo(Integer userId, Integer id, String title, Boolean completed) {
    }
}
