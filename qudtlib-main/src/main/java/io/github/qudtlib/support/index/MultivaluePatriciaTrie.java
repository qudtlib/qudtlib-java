package io.github.qudtlib.support.index;

import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.collections4.trie.PatriciaTrie;

public class MultivaluePatriciaTrie<V> {

    private final PatriciaTrie<Set<V>> delegate = new PatriciaTrie<>();

    public MultivaluePatriciaTrie() {}

    public void put(String key, V value) {
        delegate.compute(
                key,
                (k, values) -> {
                    if (values == null) {
                        values = new HashSet<>();
                    }
                    values.add(value);
                    return values;
                });
    }

    public Set<V> getByPrefixMatch(String prefix) {
        return delegate.prefixMap(prefix).values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    public Set<V> get(String k) {
        Set<V> set = delegate.get(k);
        if (set == null) {
            return Set.of();
        }
        return set.stream().collect(Collectors.toUnmodifiableSet());
    }

    public void clear() {
        delegate.clear();
    }
}
