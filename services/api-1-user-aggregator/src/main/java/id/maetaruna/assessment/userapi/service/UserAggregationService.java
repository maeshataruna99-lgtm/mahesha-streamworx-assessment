package id.maetaruna.assessment.userapi.service;

import id.maetaruna.assessment.userapi.client.PostClient;
import id.maetaruna.assessment.userapi.client.TodoClient;
import id.maetaruna.assessment.userapi.dto.PostItem;
import id.maetaruna.assessment.userapi.dto.TodoItem;
import id.maetaruna.assessment.userapi.dto.UserResponse;
import id.maetaruna.assessment.userapi.repository.UserRedisRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class UserAggregationService {

    private final UserRedisRepository userRedisRepository;
    private final PostClient postClient;
    private final TodoClient todoClient;

    public UserAggregationService(UserRedisRepository userRedisRepository, PostClient postClient,
            TodoClient todoClient) {
        this.userRedisRepository = userRedisRepository;
        this.postClient = postClient;
        this.todoClient = todoClient;
    }

    public List<UserResponse> getUsers() {
        Map<Integer, List<PostItem>> posts = postClient.postsByUserId();
        Map<Integer, List<TodoItem>> todos = todoClient.todosByUserId();
        return userRedisRepository.findAll().stream()
                .map(user -> new UserResponse(user,
                        todos.getOrDefault(user.id(), List.of()),
                        posts.getOrDefault(user.id(), List.of())))
                .toList();
    }
}
