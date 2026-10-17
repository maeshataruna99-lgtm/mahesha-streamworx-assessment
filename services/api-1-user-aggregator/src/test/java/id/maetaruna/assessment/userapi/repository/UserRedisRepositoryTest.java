package id.maetaruna.assessment.userapi.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import id.maetaruna.assessment.userapi.RedisContainerConfig;
import id.maetaruna.assessment.userapi.model.Address;
import id.maetaruna.assessment.userapi.model.Company;
import id.maetaruna.assessment.userapi.model.Geo;
import id.maetaruna.assessment.userapi.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

@DataRedisTest
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@Import({RedisContainerConfig.class, UserRedisRepository.class})
class UserRedisRepositoryTest {

    @Autowired
    UserRedisRepository userRedisRepository;

    @Autowired
    StringRedisTemplate redisTemplate;

    @BeforeEach
    void clearRedis() {
        redisTemplate.delete(UserRedisRepository.KEY);
    }

    @Test
    void returnsStoredUsersOrderedById() {
        userRedisRepository.saveIfAbsent(user(2, "Ervin Howell"));
        userRedisRepository.saveIfAbsent(user(1, "Leanne Graham"));

        assertThat(userRedisRepository.findAll())
                .extracting(User::id, User::name)
                .containsExactly(tuple(1, "Leanne Graham"), tuple(2, "Ervin Howell"));
    }

    @Test
    void keepsNestedAddressAndCompany() {
        User leanne = user(1, "Leanne Graham");
        userRedisRepository.saveIfAbsent(leanne);

        assertThat(userRedisRepository.findAll()).containsExactly(leanne);
    }

    @Test
    void doesNotOverwriteAnExistingUser() {
        assertThat(userRedisRepository.saveIfAbsent(user(1, "Leanne Graham"))).isTrue();
        assertThat(userRedisRepository.saveIfAbsent(user(1, "Someone Else"))).isFalse();

        assertThat(userRedisRepository.findAll()).extracting(User::name).containsExactly("Leanne Graham");
    }

    private static User user(int id, String name) {
        return new User(id, name, "Bret", "Sincere@april.biz",
                new Address("Kulas Light", "Apt. 556", "Gwenborough", "92998-3874", new Geo("-37.3159", "81.1496")),
                "1-770-736-8031 x56442", "hildegard.org",
                new Company("Romaguera-Crona", "Multi-layered client-server neural-net", "harness real-time e-markets"));
    }
}
