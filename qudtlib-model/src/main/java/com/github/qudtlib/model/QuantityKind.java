package com.github.qudtlib.model;

import java.util.*;

/**
 * Represents a QUDT QuantityKind.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class QuantityKind {
    private final String iri;
    private final Set<LangString> labels;
    private final Set<String> applicableUnits;
    private final Set<String> broaderQuantityKinds;
    private final String dimensionVector;
    private final String symbol;

    public QuantityKind(
            String iri,
            Set<LangString> labels,
            Set<String> applicableUnits,
            Set<String> broaderQuantityKinds,
            String dimensionVector,
            String symbol) {
        this.iri = iri;
        this.labels = labels;
        this.applicableUnits = new HashSet<>(applicableUnits);
        this.broaderQuantityKinds = new HashSet<>(broaderQuantityKinds);
        this.dimensionVector = dimensionVector;
        this.symbol = symbol;
    }

    public QuantityKind(String iri, String dimensionVector, String symbol) {
        this.iri = iri;
        this.dimensionVector = dimensionVector;
        this.symbol = symbol;
        this.applicableUnits = new HashSet<>();
        this.broaderQuantityKinds = new HashSet<>();
        this.labels = new HashSet<>();
    }

    public String getIri() {
        return iri;
    }

    public Set<String> getApplicableUnits() {
        return Collections.unmodifiableSet(applicableUnits);
    }

    public Set<String> getBroaderQuantityKinds() {
        return Collections.unmodifiableSet(broaderQuantityKinds);
    }

    public Optional<String> getDimensionVector() {
        return Optional.ofNullable(dimensionVector);
    }

    public Optional<String> getSymbol() {
        return Optional.ofNullable(symbol);
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

    public void addApplicableUnit(String unit) {
        Objects.requireNonNull(unit);
        this.applicableUnits.add(unit);
    }

    public void addBroaderQuantityKind(String quantitkyKind) {
        Objects.requireNonNull(quantitkyKind);
        this.broaderQuantityKinds.add(quantitkyKind);
    }

    @Override
    public String toString() {
        if (symbol != null) {
            return symbol;
        }
        return "quantityKind:" + iri.replaceAll(".+/([^/]+)", "$1");
    }
}
