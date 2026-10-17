package id.maetaruna.assessment.userapi.repository;

import id.maetaruna.assessment.userapi.model.User;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;

@Repository
public class UserRedisRepository {

    // One hash keyed by user id: the aggregation always needs every user, so a single HGETALL is enough.
    static final String KEY = "users";

    private final HashOperations<String, String, String> hash;
    private final JsonMapper jsonMapper;

    public UserRedisRepository(StringRedisTemplate redisTemplate, JsonMapper jsonMapper) {
        this.hash = redisTemplate.opsForHash();
        this.jsonMapper = jsonMapper;
    }

    public List<User> findAll() {
        return hash.values(KEY).stream()
                .map(json -> jsonMapper.readValue(json, User.class))
                .sorted(Comparator.comparing(User::id))
                .toList();
    }

    public boolean saveIfAbsent(User user) {
        return hash.putIfAbsent(KEY, user.id().toString(), jsonMapper.writeValueAsString(user));
    }
}
