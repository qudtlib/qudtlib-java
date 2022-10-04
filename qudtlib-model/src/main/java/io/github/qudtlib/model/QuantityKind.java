package io.github.qudtlib.model;

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
    private final Set<String> applicableUnitIris;
    private final Set<String> broaderQuantityKindIris;
    private final String dimensionVectorIri;
    private final String symbol;

    public QuantityKind(
            String iri,
            Set<LangString> labels,
            Set<String> applicableUnitIris,
            Set<String> broaderQuantityKindIris,
            String dimensionVectorIri,
            String symbol) {
        this.iri = iri;
        this.labels = labels;
        this.applicableUnitIris = new HashSet<>(applicableUnitIris);
        this.broaderQuantityKindIris = new HashSet<>(broaderQuantityKindIris);
        this.dimensionVectorIri = dimensionVectorIri;
        this.symbol = symbol;
    }

    public QuantityKind(String iri, String dimensionVectorIri, String symbol) {
        this.iri = iri;
        this.dimensionVectorIri = dimensionVectorIri;
        this.symbol = symbol;
        this.applicableUnitIris = new HashSet<>();
        this.broaderQuantityKindIris = new HashSet<>();
        this.labels = new HashSet<>();
    }

    public String getIri() {
        return iri;
    }

    public Set<String> getApplicableUnitIris() {
        return Collections.unmodifiableSet(applicableUnitIris);
    }

    public Set<String> getBroaderQuantityKindIris() {
        return Collections.unmodifiableSet(broaderQuantityKindIris);
    }

    public Optional<String> getDimensionVectorIri() {
        return Optional.ofNullable(dimensionVectorIri);
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

    public void addApplicableUnitIri(String unit) {
        Objects.requireNonNull(unit);
        this.applicableUnitIris.add(unit);
    }

    public void addBroaderQuantityKindIri(String quantitkyKindIri) {
        Objects.requireNonNull(quantitkyKindIri);
        this.broaderQuantityKindIris.add(quantitkyKindIri);
    }

    @Override
    public String toString() {
        if (symbol != null) {
            return symbol;
        }
        return "quantityKind:" + iri.replaceAll(".+/([^/]+)", "$1");
    }
}
