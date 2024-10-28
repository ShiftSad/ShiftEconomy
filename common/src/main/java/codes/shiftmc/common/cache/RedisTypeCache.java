package codes.shiftmc.common.cache;

import io.lettuce.core.api.reactive.RedisReactiveCommands;
import reactor.core.publisher.Mono;

public class RedisTypeCache<T> implements TypeCache<T> {

    private final RedisReactiveCommands<String, T> redisCommands;

    public RedisTypeCache(RedisReactiveCommands<String, T> redisCommands) {
        this.redisCommands = redisCommands;
    }

    @Override
    public Mono<T> get(String key) {
        return redisCommands.get(key);
    }

    @Override
    public Mono<T> set(String key, T value, long expirationTime) {
        return redisCommands.setex(key, expirationTime, value).thenReturn(value);
    }
}