package io.github.qudtlib;

import io.github.qudtlib.model.QudtNamespaces;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NamespaceTests {

    @Test
    public void testMakeNamespaceIri() {
        String mIriFull = QudtNamespaces.unit.makeIriInNamespace("M");
        Assertions.assertEquals("http://qudt.org/vocab/unit/M", mIriFull);
    }

    @Test
    public void testAbbreviate() {
        String mIriAbbreviated = QudtNamespaces.unit.abbreviate("http://qudt.org/vocab/unit/M");
        Assertions.assertEquals("unit:M", mIriAbbreviated);
    }

    @Test
    public void testExpand() {
        String mIriFull = QudtNamespaces.unit.expand("unit:M");
        Assertions.assertEquals("http://qudt.org/vocab/unit/M", mIriFull);
    }
}
