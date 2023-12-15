package io.github.qudtlib.tools.contribute.support.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class NodeTests {
    @Test
    public void testBuilder() {
        Node<String> root =
                Node.builder("top")
                        .leaf("leaf1")
                        .leaf("leaf2")
                        .inner("inner")
                        .leaf("leaf3")
                        .up()
                        .leaf("leaf4")
                        .build();
        assertEquals(4, root.getChildrenCount());
        assertEquals("top", root.getData());
        assertEquals(6, root.size());
    }

    @ParameterizedTest
    @MethodSource
    public void testForestOf(List<Node<String>> expectedResult, List<String> items) {
        List<Node<String>> forest =
                Node.forestOf(
                        items,
                        (parentCandidate, childCandidate) -> {
                            return childCandidate.matches(parentCandidate + ".[^\\.]+");
                        });
        assertEquals(expectedResult, forest);
    }

    public static Stream<Arguments> testForestOf() {
        return Stream.of(
                Arguments.of(
                        List.of(Node.builder("top1").leaf("top1.leaf1").build()),
                        List.of("top1", "top1.leaf1")),
                Arguments.of(
                        List.of(Node.builder("top1").leaf("top1.leaf1").build()),
                        List.of("top1.leaf1", "top1")),
                Arguments.of(
                        List.of(Node.builder("top1").leaf("top1.leaf1").leaf("top1.leaf2").build()),
                        List.of("top1.leaf1", "top1", "top1.leaf2")),
                Arguments.of(
                        List.of(Node.builder("top1").leaf("top1.leaf1").leaf("top1.leaf2").build()),
                        List.of("top1.leaf1", "top1.leaf2", "top1")),
                Arguments.of(
                        List.of(
                                Node.builder("top1")
                                        .leaf("top1.leaf1")
                                        .leaf("top1.leaf2")
                                        .inner("top1.inner1")
                                        .leaf("top1.inner1.leaf1")
                                        .leaf("top1.inner1.leaf2")
                                        .build()),
                        List.of(
                                "top1.leaf1",
                                "top1",
                                "top1.leaf2",
                                "top1.inner1",
                                "top1.inner1.leaf1",
                                "top1.inner1.leaf2")),
                Arguments.of(
                        List.of(
                                Node.builder("top1")
                                        .leaf("top1.leaf1")
                                        .leaf("top1.leaf2")
                                        .inner("top1.inner1")
                                        .leaf("top1.inner1.leaf1")
                                        .leaf("top1.inner1.leaf2")
                                        .build(),
                                Node.builder("top2")
                                        .leaf("top2.leaf1")
                                        .leaf("top2.leaf2")
                                        .inner("top2.inner1")
                                        .leaf("top2.inner1.leaf1")
                                        .build()),
                        List.of(
                                "top1.leaf1",
                                "top1",
                                "top1.leaf2",
                                "top1.inner1",
                                "top1.inner1.leaf1",
                                "top1.inner1.leaf2",
                                "top2",
                                "top2.leaf1",
                                "top2.leaf2",
                                "top2.inner1",
                                "top2.inner1.leaf1")),
                Arguments.of(
                        List.of(
                                Node.builder("top2")
                                        .leaf("top2.leaf1")
                                        .leaf("top2.leaf2")
                                        .inner("top2.inner1")
                                        .leaf("top2.inner1.leaf1")
                                        .build(),
                                Node.builder("top1")
                                        .leaf("top1.leaf1")
                                        .leaf("top1.leaf2")
                                        .inner("top1.inner1")
                                        .leaf("top1.inner1.leaf1")
                                        .leaf("top1.inner1.leaf2")
                                        .build()),
                        List.of(
                                "top1.leaf1",
                                "top2.leaf1",
                                "top2.inner1",
                                "top1",
                                "top1.leaf2",
                                "top1.inner1",
                                "top2",
                                "top2.leaf2",
                                "top2.inner1.leaf1",
                                "top1.inner1.leaf1",
                                "top1.inner1.leaf2")));
    }
}
