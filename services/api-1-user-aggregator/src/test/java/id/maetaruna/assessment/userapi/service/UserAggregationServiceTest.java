package id.maetaruna.assessment.userapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import id.maetaruna.assessment.userapi.client.PostClient;
import id.maetaruna.assessment.userapi.client.TodoClient;
import id.maetaruna.assessment.userapi.dto.PostItem;
import id.maetaruna.assessment.userapi.dto.TodoItem;
import id.maetaruna.assessment.userapi.dto.UserResponse;
import id.maetaruna.assessment.userapi.model.User;
import id.maetaruna.assessment.userapi.repository.UserRedisRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UserAggregationServiceTest {

    private final UserRedisRepository users = mock(UserRedisRepository.class);
    private final PostClient postClient = mock(PostClient.class);
    private final TodoClient todoClient = mock(TodoClient.class);
    private final UserAggregationService service = new UserAggregationService(users, postClient, todoClient);

    private final User leanne = new User(1, "Leanne Graham", "Bret", "Sincere@april.biz", null, null, null, null);
    private final User ervin = new User(2, "Ervin Howell", "Antonette", "Shanna@melissa.tv", null, null, null, null);

    @Test
    void attachesEachUsersOwnTodosAndPosts() {
        given(users.findAll()).willReturn(List.of(leanne, ervin));
        given(todoClient.todosByUserId()).willReturn(Map.of(
                1, List.of(new TodoItem(1, "delectus aut autem", false)),
                2, List.of(new TodoItem(21, "suscipit repellat", true))));
        given(postClient.postsByUserId()).willReturn(Map.of(
                1, List.of(new PostItem(1, "sunt aut facere", "quia et suscipit"), new PostItem(2, "qui est esse", "est"))));

        List<UserResponse> result = service.getUsers();

        assertThat(result).extracting(UserResponse::user).containsExactly(leanne, ervin);
        assertThat(result.get(0).todos()).containsExactly(new TodoItem(1, "delectus aut autem", false));
        assertThat(result.get(0).posts()).extracting(PostItem::id).containsExactly(1, 2);
        assertThat(result.get(1).todos()).containsExactly(new TodoItem(21, "suscipit repellat", true));
    }

    @Test
    void givesEmptyListsToUsersWithoutTodosOrPosts() {
        given(users.findAll()).willReturn(List.of(ervin));
        given(todoClient.todosByUserId()).willReturn(Map.of());
        given(postClient.postsByUserId()).willReturn(Map.of());

        UserResponse only = service.getUsers().getFirst();

        assertThat(only.todos()).isEmpty();
        assertThat(only.posts()).isEmpty();
    }
}
