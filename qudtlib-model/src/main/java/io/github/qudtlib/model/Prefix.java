package io.github.qudtlib.model;

import java.math.BigDecimal;
import java.util.*;

/**
 * Represents a QUDT Prefix.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class Prefix extends SelfSmuggler {

    public static Definition definition(String iri) {
        return new Definition(iri);
    }

    public static Definition definition(Prefix prefix) {
        return new Definition(prefix);
    }

    static class Definition extends NodeDefinitionBase<String, Prefix> {
        private final String iri;
        private BigDecimal multiplier;
        private String symbol;
        private String ucumCode;
        private Set<LangString> labels = new HashSet<>();

        public Definition(String iri) {
            super(iri);
            Objects.requireNonNull(iri);
            this.iri = iri;
        }

        public Definition(Prefix presetPrefix) {
            super(presetPrefix.getIri(), presetPrefix);
            Objects.requireNonNull(presetPrefix.getIri());
            this.iri = presetPrefix.getIri();
        }

        public Definition multiplier(BigDecimal multiplier) {
            doIfPresent(multiplier, m -> this.multiplier = m);
            return this;
        }

        public Definition symbol(String symbol) {
            doIfPresent(symbol, s -> this.symbol = s);
            return this;
        }

        public Definition ucumCode(String ucumCode) {
            doIfPresent(ucumCode, s -> this.ucumCode = s);
            return this;
        }

        public Definition addLabel(LangString label) {
            doIfPresent(label, l -> this.labels.add(l));
            return this;
        }

        public Prefix doBuild() {
            return new Prefix(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Definition definition = (Definition) o;
            return iri.equals(definition.iri);
        }

        @Override
        public int hashCode() {
            return Objects.hash(iri);
        }
    }

    private final String iri;
    private final BigDecimal multiplier;
    private final String symbol;
    private final String ucumCode;
    private final LangStrings labels;

    public Prefix(Definition definition) {
        super(definition);
        Objects.requireNonNull(definition.iri);
        Objects.requireNonNull(definition.iri);
        Objects.requireNonNull(definition.multiplier);
        Objects.requireNonNull(definition.symbol);
        Objects.requireNonNull(definition.labels);
        this.iri = definition.iri;
        this.multiplier = definition.multiplier;
        this.symbol = definition.symbol;
        this.labels = new LangStrings(definition.labels);
        this.ucumCode = definition.ucumCode;
    }

    public String getIri() {
        return iri;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public String getSymbol() {
        return symbol;
    }

    public Optional<String> getUcumCode() {
        return Optional.ofNullable(ucumCode);
    }

    public Set<LangString> getLabels() {
        return labels.getAll();
    }

    public Optional<LangString> getLabelForLanguageTag(String languageTag) {
        return labels.getLangStringForLanguageTag(languageTag, null, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prefix prefix = (Prefix) o;
        return multiplier.compareTo(prefix.multiplier) == 0
                && Objects.equals(iri, prefix.iri)
                && Objects.equals(symbol, prefix.symbol)
                && Objects.equals(ucumCode, prefix.ucumCode)
                && Objects.equals(labels, prefix.labels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iri, multiplier, symbol, ucumCode, labels);
    }

    @Override
    public String toString() {
        return "prefix:" + iri.replaceAll(".+/", "");
    }
}
