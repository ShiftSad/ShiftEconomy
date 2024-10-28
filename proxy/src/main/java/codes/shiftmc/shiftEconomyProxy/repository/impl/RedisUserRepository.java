package codes.shiftmc.shiftEconomyProxy.repository.impl;

import codes.shiftmc.shiftEconomyProxy.model.UserData;
import codes.shiftmc.shiftEconomyProxy.repository.UserRepository;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

public class RedisUserRepository implements UserRepository {

    private final UserRepository primaryRepository;
    private final RedisReactiveCommands<String, UserData> redisCommands;
    private static final String USER_CACHE_KEY_PREFIX = "user:";

    public RedisUserRepository(UserRepository primaryRepository, RedisReactiveCommands<String, UserData> redisCommands) {
        this.primaryRepository = primaryRepository;
        this.redisCommands = redisCommands;
    }

    private String cacheKey(UUID uuid) {
        return USER_CACHE_KEY_PREFIX + uuid.toString();
    }

    @Override
    public Mono<UserData> findByUuid(UUID uuid) {
        return redisCommands.get(cacheKey(uuid))
                .flatMap(Mono::just)
                .switchIfEmpty(
                        primaryRepository.findByUuid(uuid)
                                .flatMap(user -> redisCommands.set(cacheKey(uuid), user)
                                        .thenReturn(user))
                );
    }

    @Override
    public Mono<UserData> findByUsername(String username) {
        return primaryRepository.findByUsername(username)
                .flatMap(user -> redisCommands.set(cacheKey(user.getUuid()), user).thenReturn(user));
    }

    @Override
    public Mono<UserData> save(UserData userData) {
        return primaryRepository.save(userData)
                .flatMap(user -> redisCommands.set(cacheKey(user.getUuid()), user).thenReturn(user));
    }

    @Override
    public Mono<UserData> updateBalance(UUID uuid, double newBalance) {
        return primaryRepository.updateBalance(uuid, newBalance)
                .flatMap(user -> redisCommands.set(cacheKey(user.getUuid()), user).thenReturn(user));
    }
}