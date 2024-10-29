package codes.shiftmc.common.service;

import codes.shiftmc.common.cache.TypeCache;
import codes.shiftmc.common.model.UserData;
import codes.shiftmc.common.repository.UserRepository;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@AllArgsConstructor
public class UserService {

    private static final String USER_CACHE_KEY_PREFIX = "user:";
    private static final long CACHE_EXPIRATION_TIME = 3600; // 1 hour in seconds

    private final UserRepository userRepository;
    private final TypeCache<UserData> cache;

    private static String cacheKey(UUID uuid) {
        return USER_CACHE_KEY_PREFIX + uuid.toString();
    }

    /**
     * Finds a user by their UUID.
     *
     * @param uuid The UUID of the user.
     * @return A Mono emitting the UserData if found, empty if not.
     */
    public Mono<UserData> findByUuid(UUID uuid) {
        if (cache != null)
            return cache.get(cacheKey(uuid))
                .switchIfEmpty(userRepository.findByUuid(uuid)
                        .flatMap(user -> cache.set(cacheKey(uuid), user, CACHE_EXPIRATION_TIME)));
        else return userRepository.findByUuid(uuid);
    }

    /**
     * Finds a user by their username.
     *
     * @param username The username of the user.
     * @return A Mono emitting the UserData if found, empty if not.
     */
    public Mono<UserData> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .flatMap(user -> {
                    if (cache != null) return cache.set(cacheKey(user.getUUID()), user, CACHE_EXPIRATION_TIME).thenReturn(user);
                    else return Mono.just(user);
                });
    }

    /**
     * Saves a new user or updates an existing user.
     *
     * @param userData The UserData to save or update.
     * @return A Mono emitting the saved or updated UserData.
     */
    public Mono<UserData> save(UserData userData) {
        return userRepository.findByUuid(userData.getUUID())
                .flatMap(user -> {
                    userRepository.updateBalance(user.getUUID(), userData.getBalance());
                    if (cache != null) return cache.set(cacheKey(user.getUUID()), user, CACHE_EXPIRATION_TIME).thenReturn(user);
                    else return Mono.just(user);
                })
                .switchIfEmpty(userRepository.save(userData));
    }

    /**
     * Updates a user's balance.
     *
     * @param uuid The UUID of the user.
     * @param newBalance The new balance amount.
     * @return A Mono emitting the updated UserData.
     */
    public Mono<UserData> updateBalance(UUID uuid, double newBalance) {
        return userRepository.findByUuid(uuid)
                .flatMap(user -> {
                    user.setBalance(newBalance);
                    return userRepository.updateBalance(uuid, newBalance)
                            .then(Mono.just(user));
                })
                .flatMap(user -> {
                    if (cache != null) return cache.set(cacheKey(user.getUUID()), user, CACHE_EXPIRATION_TIME).thenReturn(user);
                    else return Mono.just(user);
                });
    }
}