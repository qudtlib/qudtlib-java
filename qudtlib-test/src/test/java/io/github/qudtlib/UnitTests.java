package io.github.qudtlib;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class UnitTests {

    @Test
    public void testDescription() {
        assertTrue(Qudt.Units.M.getDescription().isPresent());
    }
}
