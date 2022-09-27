package io.github.qudtlib.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.qudlib.common.CodeGen;
import io.github.qudlib.common.safenames.NameCollisionException;
import io.github.qudlib.common.safenames.SafeStringMapper;
import org.junit.jupiter.api.Test;

public class CodeGenTest {
    @Test
    public void testConstantMapper() {
        String runIdentifier = "run1";
        SafeStringMapper mapper = CodeGen.javaConstantMapper();
        assertEquals("_1constant", mapper.applyMapping("1constant"));
        assertEquals("_1constant", mapper.applyMapping("1constant"));
        assertEquals("_2constant", mapper.applyMapping("2constant"));
        assertThrows(NameCollisionException.class, () -> mapper.applyMapping("_1constant"));
    }
}
