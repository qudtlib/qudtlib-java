package io.github.qudtlib.model;

import static io.github.qudtlib.nodedef.Builder.buildSet;

import io.github.qudtlib.nodedef.Builder;
import io.github.qudtlib.nodedef.NodeDefinitionBase;
import io.github.qudtlib.nodedef.SelfSmuggler;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a QUDT QuantityKind.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class QuantityKind extends SelfSmuggler {
    public static Definition definition(String iri) {
        return new Definition(iri);
    }

    public static Definition definition(QuantityKind quantityKind) {
        return new Definition(quantityKind);
    }

    public static class Definition extends NodeDefinitionBase<String, QuantityKind> {
        private final String iri;
        private Set<LangString> labels = new HashSet<>();
        private Set<Builder<Unit>> applicableUnits = new HashSet<>();
        private Set<Builder<QuantityKind>> broaderQuantityKinds = new HashSet<>();
        private String dimensionVectorIri;
        private String symbol;

        public Definition(String iri) {
            super(iri);
            this.iri = iri;
        }

        public Definition(QuantityKind presetProduct) {
            super(presetProduct.getIri(), presetProduct);
            this.iri = presetProduct.getIri();
        }

        public Definition label(LangString label) {
            doIfPresent(label, l -> this.labels.add(label));
            return this;
        }

        public Definition dimensionVectorIri(String dimensionVectorIri) {
            doIfPresent(dimensionVectorIri, d -> this.dimensionVectorIri = dimensionVectorIri);
            return this;
        }

        public Definition symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Definition addLabel(LangString label) {
            doIfPresent(label, l -> this.labels.add(label));
            return this;
        }

        public Definition addLabel(String label, String languageTag) {
            if (label != null) {
                this.labels.add(new LangString(label, languageTag));
            }
            return this;
        }

        public Definition addApplicableUnit(Builder<Unit> applicableUnit) {
            doIfPresent(applicableUnit, a -> this.applicableUnits.add(applicableUnit));
            return this;
        }

        public Definition addBroaderQuantityKind(Builder<QuantityKind> broaderQuantityKind) {
            doIfPresent(
                    broaderQuantityKind, b -> this.broaderQuantityKinds.add(broaderQuantityKind));
            return this;
        }

        @Override
        protected QuantityKind doBuild() {
            return new QuantityKind(this);
        }
    }

    private final String iri;
    private final LangStrings labels;
    private final Set<Unit> applicableUnits;
    private final Set<QuantityKind> broaderQuantityKinds;
    private final String dimensionVectorIri;
    private final String symbol;

    public QuantityKind(Definition definition) {
        super(definition);
        Objects.requireNonNull(definition.iri);
        Objects.requireNonNull(definition.applicableUnits);
        this.iri = definition.iri;
        this.labels = new LangStrings(definition.labels);
        this.dimensionVectorIri = definition.dimensionVectorIri;
        this.symbol = definition.symbol;
        this.broaderQuantityKinds = buildSet(definition.broaderQuantityKinds);
        this.applicableUnits = buildSet(definition.applicableUnits);
    }

    public String getIri() {
        return iri;
    }

    public Set<Unit> getApplicableUnits() {
        return this.applicableUnits;
    }

    public Set<QuantityKind> getBroaderQuantityKinds() {
        return this.broaderQuantityKinds;
    }

    public Optional<String> getDimensionVectorIri() {
        return Optional.ofNullable(dimensionVectorIri);
    }

    public Optional<String> getSymbol() {
        return Optional.ofNullable(symbol);
    }

    public Set<LangString> getLabels() {
        return labels.getAll();
    }

    public Optional<LangString> getLabelForLanguageTag(String languageTag) {
        return labels.getLangStringForLanguageTag(languageTag, null, false);
    }

    public boolean hasLabel(String label) {
        return labels.containsString(label);
    }

    @Override
    public String toString() {
        if (symbol != null) {
            return symbol;
        }
        return "quantityKind:" + iri.replaceAll(".+/([^/]+)", "$1");
    }
}
