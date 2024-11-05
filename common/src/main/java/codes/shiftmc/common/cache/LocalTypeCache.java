package codes.shiftmc.common.cache;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
public class LocalTypeCache<T> implements TypeCache<T> {

    private final Map<String, CacheEntry<T>> localCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<List<T>>> listCache = new ConcurrentHashMap<>();

    @Override
    public Mono<T> get(String key) {
        return Mono.create(sink -> {
            CacheEntry<T> entry = localCache.get(key);
            if (entry == null || entry.isExpired()) {
                localCache.remove(key);
                sink.success();
            } else {
                sink.success(entry.value());
            }
        });
    }

    @Override
    public Mono<T> set(String key, T value, long expirationTime) {
        long expirationTimestamp = System.currentTimeMillis() + (expirationTime * 1000);
        CacheEntry<T> entry = new CacheEntry<>(value, expirationTimestamp);
        localCache.put(key, entry);
        return Mono.just(value);
    }

    @Override
    public Mono<List<T>> getList(String key) {
        return Mono.create(sink -> {
            var entry = listCache.get(key);
            if (entry == null || entry.isExpired()) {
                localCache.remove(key);
                sink.success(Collections.emptyList());
            } else {
                sink.success(entry.value());
            }
        });
    }

    @Override
    public Mono<List<T>> setList(String key, List<T> value, long expirationTime) {
        long expirationTimestamp = System.currentTimeMillis() + (expirationTime * 1000);
        CacheEntry<List<T>> entry = new CacheEntry<>(value, expirationTimestamp);
        listCache.put(key, entry);
        return Mono.just(value);
    }

    private record CacheEntry<V>(V value, long expirationTimestamp) {
        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTimestamp;
        }
    }
}