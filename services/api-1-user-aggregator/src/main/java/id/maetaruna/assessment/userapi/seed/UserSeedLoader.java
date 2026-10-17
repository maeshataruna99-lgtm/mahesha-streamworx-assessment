package id.maetaruna.assessment.userapi.seed;

import id.maetaruna.assessment.userapi.model.User;
import id.maetaruna.assessment.userapi.repository.UserRedisRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Component
public class UserSeedLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserSeedLoader.class);
    private static final String SEED_FILE = "seed/users.json";

    private final UserRedisRepository userRedisRepository;
    private final JsonMapper jsonMapper;

    public UserSeedLoader(UserRedisRepository userRedisRepository, JsonMapper jsonMapper) {
        this.userRedisRepository = userRedisRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        List<User> users;
        try (InputStream in = new ClassPathResource(SEED_FILE).getInputStream()) {
            users = jsonMapper.readValue(in, new TypeReference<>() {
            });
        }
        long inserted = users.stream().filter(userRedisRepository::saveIfAbsent).count();
        log.info("User seed: {} inserted, {} already present", inserted, users.size() - inserted);
    }
}
