package io.github.qudtlib.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.qudtlib.nodedef.Builder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Note: For these tests, we need to stub or mock dependencies like Unit, DimensionVector, etc.
// Assuming Unit has a builder and convert method as inferred from QuantityValue usage.
// We'll create minimal stub classes or use builders where possible for isolation.

class QuantityKindTests {

    private QuantityKind quantityKind;
    private QuantityKind.Definition definition;

    @BeforeEach
    void setUp() {
        definition = QuantityKind.definition("http://qudt.org/vocab/quantitykind/TestKind");
    }

    @Test
    void testDefinitionConstructorWithIri() {
        QuantityKind qk = definition.build();
        assertThat(qk.getIri()).isEqualTo("http://qudt.org/vocab/quantitykind/TestKind");
        assertThat(qk.getIriLocalname()).isEqualTo("TestKind");
    }

    @Test
    void testDefinitionConstructorWithExistingQuantityKind() {
        QuantityKind original = definition.build();
        QuantityKind.Definition newDef = QuantityKind.definition(original);
        QuantityKind newQk = newDef.build();
        assertThat(newQk).isEqualToComparingFieldByField(original);
    }

    @Test
    void testAddLabel() {
        definition.addLabel("Test Label", "en");
        QuantityKind qk = definition.build();
        assertThat(qk.getLabels()).hasSize(1);
        assertThat(qk.getLabelForLanguageTag("en"))
                .isPresent()
                .contains(new LangString("Test Label", "en"));
        assertThat(qk.hasLabel("Test Label")).isTrue();
    }

    @Test
    void testAddMultipleLabels() {
        definition.addLabel("English Label", "en");
        definition.addLabel("Deutsches Label", "de");
        QuantityKind qk = definition.build();
        assertThat(qk.getLabels()).hasSize(2);
        assertThat(qk.getLabelForLanguageTag("en", "fr", true))
                .isPresent()
                .contains("English Label");
        assertThat(qk.getLabelForLanguageTag("fr", "es", false)).isEmpty();
    }

    @Test
    void testSymbol() {
        definition.symbol("L");
        QuantityKind qk = definition.build();
        assertThat(qk.getSymbol()).isPresent().contains("L");
    }

    @Test
    void testDescription() {
        definition.description("A test quantity kind");
        QuantityKind qk = definition.build();
        assertThat(qk.getDescription()).isPresent().contains("A test quantity kind");
    }

    @Test
    void testDimensionVectorIri() {
        definition.dimensionVectorIri("http://qudt.org/vocab/dimensionvector/A0E0L1I0M0H0T0D0");
        QuantityKind qk = definition.build();
        assertThat(qk.getDimensionVectorIri())
                .isPresent()
                .contains("http://qudt.org/vocab/dimensionvector/A0E0L1I0M0H0T0D0");
        assertThat(qk.getDimensionVector()).isPresent();
    }

    @Test
    void testQkdvNumeratorAndDenominatorIri() {
        definition.qkdvNumeratorIri("numIri");
        definition.qkdvDenominatorIri("denIri");
        QuantityKind qk = definition.build();
        assertThat(qk.getQkdvNumeratorIri()).isPresent().contains("numIri");
        assertThat(qk.getQkdvDenominatorIri()).isPresent().contains("denIri");
    }

    @Test
    void testAddApplicableUnit() {
        Builder<Unit> unitBuilder = Unit.definition("http://unit/Meter");
        definition.addApplicableUnit(unitBuilder);
        QuantityKind qk = definition.build();
        assertThat(qk.getApplicableUnits()).hasSize(1);
        qk.addApplicableUnit(unitBuilder.build()); // Test mutable add
        assertThat(qk.getApplicableUnits()).hasSize(1); // Unmodifiable, but add method is protected
    }

    @Test
    void testAddBroaderQuantityKind() {
        Builder<QuantityKind> broaderBuilder =
                QuantityKind.definition("http://qudt.org/vocab/quantitykind/Broader");
        definition.addBroaderQuantityKind(broaderBuilder);
        QuantityKind qk = definition.build();
        assertThat(qk.getBroaderQuantityKinds()).hasSize(1);
        qk.addBroaderQuantityKind(broaderBuilder.build());
        assertThat(qk.getBroaderQuantityKinds()).hasSize(1); // Assuming add is protected/internal
    }

    @Test
    void testAddExactMatch() {
        Builder<QuantityKind> matchBuilder =
                QuantityKind.definition("http://qudt.org/vocab/quantitykind/Match");
        definition.addExactMatch(matchBuilder);
        QuantityKind qk = definition.build();
        assertThat(qk.getExactMatches()).hasSize(1);
        qk.addExactMatches(matchBuilder.build());
        assertThat(qk.getExactMatches()).hasSize(1);
    }

    @Test
    void testDeprecated() {
        definition.deprecated(true);
        QuantityKind qk = definition.build();
        assertThat(qk.isDeprecated()).isTrue();
    }

    @Test
    void testGetDimensionVectorFromBroader() {
        QuantityKind broader =
                QuantityKind.definition("broader").dimensionVectorIri("dimIri").build();
        definition.addBroaderQuantityKind(broader);
        QuantityKind qk = definition.build();
        assertThat(qk.getDimensionVectorIri()).isPresent().contains("dimIri");
    }

    @Test
    void testToStringWithSymbol() {
        definition.symbol("Sym");
        QuantityKind qk = definition.build();
        assertThat(qk.toString()).isEqualTo("Sym");
    }

    @Test
    void testToStringWithoutSymbol() {
        QuantityKind qk = definition.build();
        assertThat(qk.toString()).isEqualTo("quantityKind:TestKind");
    }

    // Edge cases
    @Test
    void testNullIriThrowsException() {
        assertThatThrownBy(() -> new QuantityKind.Definition((String) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testEmptyLabels() {
        QuantityKind qk = definition.build();
        assertThat(qk.getLabels()).isEmpty();
        assertThat(qk.getLabelForLanguageTag("en")).isEmpty();
    }

    @Test
    void testNoDimensionVector() {
        QuantityKind qk = definition.build();
        assertThat(qk.getDimensionVector()).isEmpty();
    }
}
