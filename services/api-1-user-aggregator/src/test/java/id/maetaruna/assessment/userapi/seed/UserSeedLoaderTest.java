package id.maetaruna.assessment.userapi.seed;

import static org.assertj.core.api.Assertions.assertThat;

import id.maetaruna.assessment.userapi.RedisContainerConfig;
import id.maetaruna.assessment.userapi.model.User;
import id.maetaruna.assessment.userapi.repository.UserRedisRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.json.JsonMapper;

@DataRedisTest
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@Import({RedisContainerConfig.class, UserRedisRepository.class, UserSeedLoader.class})
class UserSeedLoaderTest {

    @Autowired
    UserRedisRepository userRedisRepository;

    @Autowired
    UserSeedLoader userSeedLoader;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    JsonMapper jsonMapper;

    @BeforeEach
    void seedFromScratch() throws Exception {
        redisTemplate.delete("users");
        userSeedLoader.run(new DefaultApplicationArguments());
    }

    @Test
    void loadsTheBundledUsers() {
        List<User> users = userRedisRepository.findAll();

        assertThat(users).hasSize(10);
        assertThat(users.getFirst().name()).isEqualTo("Leanne Graham");
        assertThat(users.getFirst().address().geo().lat()).isEqualTo("-37.3159");
        assertThat(users.getFirst().company().bs()).isEqualTo("harness real-time e-markets");
        assertThat(users.getLast().name()).isEqualTo("Clementina DuBuque");
    }

    @Test
    void runningAgainKeepsExistingUsersUntouched() throws Exception {
        User leanne = userRedisRepository.findAll().getFirst();
        User renamed = new User(leanne.id(), "Renamed In Redis", leanne.username(), leanne.email(),
                leanne.address(), leanne.phone(), leanne.website(), leanne.company());
        redisTemplate.opsForHash().put("users", "1", jsonMapper.writeValueAsString(renamed));

        userSeedLoader.run(new DefaultApplicationArguments());

        assertThat(userRedisRepository.findAll()).hasSize(10).first().isEqualTo(renamed);
    }
}
