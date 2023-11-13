package io.github.qudtlib.nodedef;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface Builder<T> {
    static <T> Set<T> buildSet(Set<Builder<T>> builders) {
        return builders.stream().map(Builder::build).collect(Collectors.toSet());
    }

    static <T> List<T> buildList(List<Builder<T>> builders) {
        return builders.stream().map(Builder::build).collect(Collectors.toList());
    }

    public T build();
}
