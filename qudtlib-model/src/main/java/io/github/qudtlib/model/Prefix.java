package io.github.qudtlib.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a QUDT Prefix.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class Prefix {
    private final String iri;
    private final BigDecimal multiplier;
    private final String symbol;
    private final String ucumCode;
    private final Set<LangString> labels;

    public Prefix(String iri, BigDecimal multiplier, String symbol, Set<LangString> labels) {
        this.iri = iri;
        this.multiplier = multiplier;
        this.symbol = symbol;
        this.labels = labels;
        this.ucumCode = null;
    }

    public Prefix(String iri, BigDecimal multiplier, String symbol, String ucumCode) {
        this.iri = iri;
        this.multiplier = multiplier;
        this.symbol = symbol;
        this.ucumCode = ucumCode;
        this.labels = new HashSet<>();
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

    public void addLabel(LangString langString) {
        this.labels.add(langString);
    }

    public Set<LangString> getLabels() {
        return labels;
    }

    public Optional<LangString> getLabelForLanguageTag(String languageTag) {
        if (languageTag == null) {
            return labels.stream().filter(s -> s.getLanguageTag().isEmpty()).findFirst();
        } else {
            return labels.stream()
                    .filter(s -> languageTag.equals(s.getLanguageTag().orElse(null)))
                    .findFirst();
        }
    }

    public boolean hasLabel(String label) {
        return labels.stream().anyMatch(s -> s.getString().equals(label));
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
