package io.github.qudtlib.model;

import static io.github.qudtlib.nodedef.Builder.buildSet;

import io.github.qudtlib.nodedef.Builder;
import io.github.qudtlib.nodedef.NodeDefinitionBase;
import io.github.qudtlib.nodedef.SelfSmuggler;
import java.util.*;

public class SystemOfUnits extends SelfSmuggler {

    public static Definition definition(String iri) {
        return new Definition(iri);
    }

    static Definition definition(SystemOfUnits sou) {
        return new Definition(sou);
    }

    public static final class Definition extends NodeDefinitionBase<String, SystemOfUnits> {
        private final String iri;
        private String abbreviation;
        private Set<LangString> labels = new HashSet<>();
        private Set<Builder<Unit>> baseUnits = new HashSet<>();

        Definition(String iri) {
            super(iri);
            this.iri = iri;
        }

        public Definition(SystemOfUnits presetProduct) {
            super(presetProduct.getIri(), presetProduct);
            this.iri = presetProduct.getIri();
        }

        public Definition abbreviation(String abbreviation) {
            if (abbreviation != null) {
                this.abbreviation = abbreviation;
            }
            return this;
        }

        Definition addLabel(String label, String languageTag) {
            if (label != null) {
                return this.addLabel(new LangString(label, languageTag));
            }
            return this;
        }

        public Definition addLabel(LangString label) {
            if (label != null) {
                this.labels.add(label);
            }
            return this;
        }

        Definition addLabels(Collection<LangString> labels) {
            if (labels != null) {
                this.labels.addAll(labels);
            }
            return this;
        }

        public Definition addBaseUnit(Builder<Unit> unitBuilder) {
            if (unitBuilder != null) {
                this.baseUnits.add(unitBuilder);
            }
            return this;
        }

        public SystemOfUnits doBuild() {
            return new SystemOfUnits(this);
        }
    }

    private final String iri;
    private final String abbreviation;
    private final LangStrings labels;
    private final Set<Unit> baseUnits;

    public SystemOfUnits(Definition definition) {
        super(definition);
        Objects.requireNonNull(definition.iri);
        Objects.requireNonNull(definition.labels);
        Objects.requireNonNull(definition.baseUnits);
        definition.setProduct(this);
        this.iri = definition.iri;
        this.abbreviation = definition.abbreviation;
        this.labels = new LangStrings(definition.labels);
        this.baseUnits = buildSet(definition.baseUnits);
    }

    public String getIri() {
        return iri;
    }

    public Optional<String> getAbbreviation() {
        return Optional.ofNullable(abbreviation);
    }

    public Set<LangString> getLabels() {
        return labels.getAll();
    }

    public Set<Unit> getBaseUnits() {
        return baseUnits;
    }

    public boolean hasBaseUnit(Unit toCheck) {
        return this.baseUnits.contains(toCheck);
    }

    public boolean allowsUnit(Unit toCheck) {
        if (hasBaseUnit(toCheck)) {
            return true;
        }
        if (toCheck.getUnitOfSystems().contains(this)) {
            return true;
        }
        // we use gram as the base unit, but SI uses KiloGM, so if we fail for GM, try KiloGM
        if (toCheck.getIri().equals(QudtNamespaces.unit.makeIriInNamespace("GM"))) {
            return this.baseUnits.stream()
                    .anyMatch(
                            bu ->
                                    bu.getIri()
                                            .equals(
                                                    QudtNamespaces.unit.makeIriInNamespace(
                                                            "KiloGM")));
        }
        if (toCheck.getScalingOf().isPresent()) {
            Unit base = toCheck.getScalingOf().get();
            return allowsUnit(base);
        }
        if (!toCheck.getFactorUnits().isEmpty()) {
            return toCheck.getFactorUnits().stream().allMatch(fu -> this.allowsUnit(fu.unit));
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemOfUnits that = (SystemOfUnits) o;
        return iri.equals(that.iri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iri);
    }
}
