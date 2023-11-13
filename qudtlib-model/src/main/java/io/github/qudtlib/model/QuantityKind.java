package io.github.qudtlib.model;

import static io.github.qudtlib.nodedef.Builder.buildSet;

import io.github.qudtlib.nodedef.Builder;
import io.github.qudtlib.nodedef.NodeDefinitionBase;
import io.github.qudtlib.nodedef.SelfSmuggler;
import java.util.*;

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

    public static Definition definition(FactorUnits factorUnits, String localName) {
        QuantityKind.Definition def =
                QuantityKind.definition(QudtNamespaces.quantityKind.makeIriInNamespace(localName));
        def.dimensionVectorIri(factorUnits.getDimensionVectorIri());
        return def;
    }

    public static class Definition extends NodeDefinitionBase<String, QuantityKind> {
        private final String iri;
        private Set<LangString> labels = new HashSet<>();
        private Set<Builder<Unit>> applicableUnits = new HashSet<>();

        private Set<Builder<QuantityKind>> broaderQuantityKinds = new HashSet<>();

        private Set<Builder<QuantityKind>> exactMatches = new HashSet<>();
        private String dimensionVectorIri;
        private String qkdvNumeratorIri;
        private String qkdvDenominatorIri;
        private String symbol;

        public Definition(String iri) {
            super(iri);
            this.iri = iri;
        }

        public Definition(QuantityKind presetProduct) {
            super(presetProduct.getIri(), presetProduct);
            this.iri = presetProduct.getIri();
        }

        public <T extends Definition> T label(LangString label) {
            doIfPresent(label, l -> this.labels.add(label));
            return (T) this;
        }

        public <T extends Definition> T dimensionVectorIri(String dimensionVectorIri) {
            doIfPresent(dimensionVectorIri, d -> this.dimensionVectorIri = dimensionVectorIri);
            return (T) this;
        }

        public <T extends Definition> T qkdvNumeratorIri(String qkdvNumeratorIri) {
            doIfPresent(qkdvNumeratorIri, d -> this.qkdvNumeratorIri = qkdvNumeratorIri);
            return (T) this;
        }

        public <T extends Definition> T qkdvDenominatorIri(String qkdvDenominatorIri) {
            doIfPresent(qkdvDenominatorIri, d -> this.qkdvDenominatorIri = qkdvDenominatorIri);
            return (T) this;
        }

        public <T extends Definition> T symbol(String symbol) {
            this.symbol = symbol;
            return (T) this;
        }

        public <T extends Definition> T addLabel(LangString label) {
            doIfPresent(label, l -> this.labels.add(label));
            return (T) this;
        }

        public <T extends Definition> T addLabel(String label, String languageTag) {
            if (label != null) {
                this.labels.add(new LangString(label, languageTag));
            }
            return (T) this;
        }

        public <T extends Definition> T addApplicableUnit(Builder<Unit> applicableUnit) {
            doIfPresent(applicableUnit, a -> this.applicableUnits.add(a));
            return (T) this;
        }

        public <T extends Definition> T addApplicableUnit(Unit applicableUnit) {
            doIfPresent(applicableUnit, a -> this.applicableUnits.add(Unit.definition(a)));
            return (T) this;
        }

        public <T extends Definition> T addBroaderQuantityKind(
                Builder<QuantityKind> broaderQuantityKind) {
            doIfPresent(broaderQuantityKind, b -> this.broaderQuantityKinds.add(b));
            return (T) this;
        }

        public <T extends Definition> T addBroaderQuantityKind(QuantityKind broaderQuantityKind) {
            doIfPresent(
                    broaderQuantityKind,
                    b -> this.broaderQuantityKinds.add(QuantityKind.definition(b)));
            return (T) this;
        }

        public <T extends Definition> T addExactMatch(Builder<QuantityKind> exactMatch) {
            doIfPresent(exactMatch, b -> this.exactMatches.add(b));
            return (T) this;
        }

        public <T extends Definition> T addExactMatch(QuantityKind exactMatch) {
            doIfPresent(exactMatch, b -> this.exactMatches.add(QuantityKind.definition(b)));
            return (T) this;
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

    private final Set<QuantityKind> exactMatches;

    private final String dimensionVectorIri;

    private String qkdvNumeratorIri;
    private String qkdvDenominatorIri;
    private final String symbol;

    protected QuantityKind(Definition definition) {
        super(definition);
        Objects.requireNonNull(definition.iri);
        Objects.requireNonNull(definition.applicableUnits);
        this.iri = definition.iri;
        this.labels = new LangStrings(definition.labels);
        this.dimensionVectorIri = definition.dimensionVectorIri;
        this.qkdvDenominatorIri = definition.qkdvDenominatorIri;
        this.qkdvNumeratorIri = definition.qkdvNumeratorIri;
        this.symbol = definition.symbol;
        this.broaderQuantityKinds = buildSet(definition.broaderQuantityKinds);
        this.applicableUnits = buildSet(definition.applicableUnits);
        this.exactMatches = buildSet(definition.exactMatches);
    }

    public String getIri() {
        return iri;
    }

    public Set<Unit> getApplicableUnits() {
        return Collections.unmodifiableSet(this.applicableUnits);
    }

    void addApplicableUnit(Unit unit) {
        Objects.requireNonNull(unit);
        this.applicableUnits.add(unit);
    }

    public Set<QuantityKind> getBroaderQuantityKinds() {
        return Collections.unmodifiableSet(this.broaderQuantityKinds);
    }

    void addBroaderQuantityKind(QuantityKind quantityKind) {
        Objects.requireNonNull(quantityKind);
        this.broaderQuantityKinds.add(quantityKind);
    }

    public Set<QuantityKind> getExactMatches() {
        return Collections.unmodifiableSet(this.exactMatches);
    }

    void addExactMatches(QuantityKind quantityKind) {
        Objects.requireNonNull(quantityKind);
        this.exactMatches.add(quantityKind);
    }

    public Optional<String> getDimensionVectorIri() {
        return Optional.ofNullable(dimensionVectorIri);
    }

    public Optional<String> getQkdvNumeratorIri() {
        return Optional.ofNullable(qkdvNumeratorIri);
    }

    public Optional<String> getQkdvDenominatorIri() {
        return Optional.ofNullable(qkdvDenominatorIri);
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

    public Optional<String> getLabelForLanguageTag(
            String language, String fallbackLanguage, boolean allowAnyIfNoMatch) {
        return labels.getStringForLanguageTag(language, fallbackLanguage, allowAnyIfNoMatch);
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
