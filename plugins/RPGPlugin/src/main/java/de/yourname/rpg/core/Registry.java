package de.yourname.rpg.core;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Registry<T> {
    private final Map<String, T> entries = new ConcurrentHashMap<>();

    public void register(String id, T entry) {
        entries.put(id, entry);
    }

    public Optional<T> get(String id) {
        return Optional.ofNullable(entries.get(id));
    }

    public Collection<T> all() {
        return entries.values();
    }

    public void clear() {
        entries.clear();
    }
}
