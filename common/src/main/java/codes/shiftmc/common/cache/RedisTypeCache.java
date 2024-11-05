package codes.shiftmc.common.cache;

import codes.shiftmc.common.model.UserData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@AllArgsConstructor
public class RedisTypeCache<T> implements TypeCache<T> {

    private final RedisReactiveCommands<String, String> redisCommands;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Class<T> clazz;

    @Override
    public Mono<T> get(String key) {
        return redisCommands.get(key)
                .handle((json, sink) -> {
                    try {
                        sink.next(objectMapper.readValue(json, clazz));
                    } catch (JsonProcessingException e) {
                        sink.error(new RuntimeException("Failed to deserialize object", e));
                    }
                });
    }

    @Override
    public Mono<T> set(String key, T value, long expirationTime) {
        try {
            String json = objectMapper.writeValueAsString(value);
            return redisCommands.setex(key, expirationTime, json)
                    .thenReturn(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object", e);
        }
    }

    @Override
    public Mono<List<T>> getList(String key) {
        return redisCommands.get(key)
                .flatMap(json -> {
                    try {
                        List<T> users = objectMapper.readValue(json, new TypeReference<>() {});
                        return Mono.just(users);
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Failed to deserialize list", e));
                    }
                });
    }

    @Override
    public Mono<List<T>> setList(String key, List<T> list, long expirationTime) {
        try {
            String json = objectMapper.writeValueAsString(list);
            return redisCommands.setex(key, expirationTime, json)
                    .thenReturn(list);
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Failed to serialize list", e));
        }
    }
}