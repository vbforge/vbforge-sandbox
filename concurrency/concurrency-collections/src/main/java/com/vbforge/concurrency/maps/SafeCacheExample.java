package com.vbforge.concurrency.maps;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class SafeCacheExample<K, V> {
    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();
    private final Function<K, V> loader;

    public SafeCacheExample(Function<K, V> loader) {
        this.loader = loader;
    }

    public V get(K key) {
        return cache.computeIfAbsent(key, k -> {
            V value = loader.apply(k);
            if (value == null) {
                throw new IllegalStateException("Loader must not return null");
            }
            return value;
        });
    }

    public int size() {
        return cache.size();
    }

    public void clear() {
        cache.clear();
    }
}
