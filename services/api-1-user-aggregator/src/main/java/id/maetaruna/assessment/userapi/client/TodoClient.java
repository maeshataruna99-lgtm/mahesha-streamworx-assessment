package id.maetaruna.assessment.userapi.client;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import id.maetaruna.assessment.userapi.dto.TodoItem;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class TodoClient {

    private static final Logger log = LoggerFactory.getLogger(TodoClient.class);
    private static final int WARM_UP_ATTEMPTS = 5;
    private static final Duration WARM_UP_PAUSE = Duration.ofSeconds(1);

    private final RestClient restClient;
    private volatile Map<Integer, List<TodoItem>> lastSuccessful = Map.of();

    public TodoClient(@Qualifier("todoRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    // Todos come from a public service outside our control, so users are still returned when it is
    // slow or down: with the last todos fetched successfully, or none if no call has succeeded yet.
    // All todos are fetched in one call and grouped here instead of calling /todos?userId= per user.
    public Map<Integer, List<TodoItem>> todosByUserId() {
        fetch();
        return lastSuccessful;
    }

    @EventListener(ApplicationReadyEvent.class)
    void warmUpOnStartup() {
        warmUp(WARM_UP_ATTEMPTS, WARM_UP_PAUSE);
    }

    // Connections to the public service were measured failing often from some networks, so the
    // first successful fetch is retried in the background instead of being left to the first request.
    Thread warmUp(int attempts, Duration pause) {
        return Thread.ofVirtual().name("todos-warm-up").start(() -> {
            for (int attempt = 1; attempt <= attempts && !fetch(); attempt++) {
                try {
                    Thread.sleep(pause);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
    }

    private boolean fetch() {
        List<Todo> todos;
        try {
            todos = restClient.get()
                    .uri("/todos")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (RestClientException e) {
            log.warn("Todos service unavailable, using last successful result: {}", e.getMessage());
            return false;
        }
        if (todos == null) {
            return false;
        }
        lastSuccessful = todos.stream().collect(groupingBy(
                Todo::userId,
                mapping(todo -> new TodoItem(todo.id(), todo.title(), todo.completed()), toList())));
        return true;
    }

    record Todo(Integer userId, Integer id, String title, Boolean completed) {
    }
}
