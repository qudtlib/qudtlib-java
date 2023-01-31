package io.github.qudtlib.model;

import static io.github.qudtlib.model.Builder.buildSet;

import java.util.*;

public class SystemOfUnits extends SelfSmuggler {

    static Definition definition(String iri) {
        return new Definition(iri);
    }

    static Definition definition(SystemOfUnits sou) {
        return new Definition(sou);
    }

    static final class Definition extends NodeDefinitionBase<String, SystemOfUnits> {
        private final String iri;
        private String abbreviation;
        private Set<LangString> labels = new HashSet<>();
        private Set<io.github.qudtlib.model.Builder<Unit>> baseUnits = new HashSet<>();

        Definition(String iri) {
            super(iri);
            this.iri = iri;
        }

        public Definition(SystemOfUnits presetProduct) {
            super(presetProduct.getIri(), presetProduct);
            this.iri = presetProduct.getIri();
        }

        Definition abbreviation(String abbreviation) {
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

        Definition addLabel(LangString label) {
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

        Definition addBaseUnit(io.github.qudtlib.model.Builder<Unit> unitBuilder) {
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

    public String getAbbreviation() {
        return abbreviation;
    }

    public Set<LangString> getLabels() {
        return labels.getAll();
    }

    public Set<Unit> getBaseUnits() {
        return baseUnits;
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
