package codes.shiftmc.common.cache;

import reactor.core.publisher.Mono;

import java.util.List;

public interface TypeCache<T> {

    /**
     * Retrieves an object from the cache by key.
     *
     * @param key The key to retrieve the object.
     * @return A Mono emitting the cached object if found, empty if not.
     */
    Mono<T> get(String key);

    /**
     * Saves an object to the cache.
     *
     * @param key The key to save the object.
     * @param value The object to save.
     * @return A Mono emitting the saved object.
     */
    Mono<T> set(String key, T value, long expirationTime);

    Mono<List<T>> getList(String key);

    Mono<List<T>> setList(String key, List<T> value, long expirationTime);
}
