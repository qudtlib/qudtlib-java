package io.github.qudtlib;

import io.github.qudtlib.model.Unit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CurrencyTests {

    @Test
    public void testCurrency() {
        Unit euro = Qudt.Units.CCY_EUR;
        Assertions.assertTrue(euro.hasLabel("Euro"));
    }
}
