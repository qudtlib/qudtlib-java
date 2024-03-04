package io.github.qudtlib.model;

import io.github.qudtlib.nodedef.Builder;
import io.github.qudtlib.nodedef.NodeDefinitionBase;
import io.github.qudtlib.nodedef.SelfSmuggler;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ConstantValue extends SelfSmuggler {

    private final String iri;

    private final Unit unit;
    private final BigDecimal value;

    private final BigDecimal standardUncertainty;

    private final LangStrings labels;

    private final boolean deprecated;

    public ConstantValue(Definition definition) {
        super(definition);
        this.iri = definition.iri;
        this.unit = definition.unitDef.build();
        this.value = definition.value;
        this.standardUncertainty = definition.standardUncertainty;
        this.labels = new LangStrings(definition.labels);
        this.deprecated = definition.deprecated;
    }

    public static Definition definition(String iri) {
        return new Definition(iri);
    }

    public static Definition definition(ConstantValue product) {
        return new Definition(product);
    }

    public static class Definition extends NodeDefinitionBase<String, ConstantValue> {
        private String iri = null;

        private Builder<Unit> unitDef = null;

        private BigDecimal value = null;

        private BigDecimal standardUncertainty = null;

        private boolean deprecated = false;

        private Set<LangString> labels = new HashSet<>();

        public Definition(String id, ConstantValue presetProduct) {
            super(id, presetProduct);
            this.iri = id;
        }

        public Definition(ConstantValue product) {
            this(product.getIri(), product);
        }

        public Definition(String id) {
            super(id);
            this.iri = id;
        }

        @Override
        protected ConstantValue doBuild() {
            return new ConstantValue(this);
        }

        public <T extends Definition> T iri(String iri) {
            this.iri = iri;
            return (T) this;
        }

        public <T extends Definition> T unit(Builder<Unit> unitDef) {
            this.unitDef = unitDef;
            return (T) this;
        }

        public <T extends Definition> T unit(Unit unit) {
            this.unitDef = Unit.definition(unit);
            return (T) this;
        }

        public <T extends Definition> T value(BigDecimal value) {
            this.value = value;
            return (T) this;
        }

        public <T extends Definition> T value(String value) {
            this.value = new BigDecimal(value);
            return (T) this;
        }

        public <T extends Definition> T standardUncertainty(BigDecimal standardUncertainty) {
            this.standardUncertainty = standardUncertainty;
            return (T) this;
        }

        public <T extends Definition> T standardUncertainty(String standardUncertainty) {
            if (standardUncertainty != null) {
                return this.standardUncertainty(new BigDecimal(standardUncertainty));
            }
            return (T) this;
        }

        public <T extends Definition> T addLabel(String label, String languageTag) {
            if (label != null) {
                return this.addLabel(new LangString(label, languageTag));
            }
            return (T) this;
        }

        public <T extends Definition> T addLabel(String label) {
            if (label != null) {
                return this.addLabel(new LangString(label));
            }
            return (T) this;
        }

        public <T extends Definition> T addLabel(LangString label) {
            doIfPresent(label, l -> this.labels.add(l));
            return (T) this;
        }

        public <T extends Definition> T addLabels(Collection<LangString> labels) {
            this.labels.addAll(labels);
            return (T) this;
        }

        public <T extends Definition> T deprecated(boolean isDeprecated) {
            this.deprecated = isDeprecated;
            return (T) this;
        }
    }

    public String getIri() {
        return iri;
    }

    public Unit getUnit() {
        return unit;
    }

    public BigDecimal getValue() {
        return value;
    }

    public Optional<BigDecimal> getStandardUncertainty() {
        return Optional.ofNullable(standardUncertainty);
    }

    public Set<LangString> getLabels() {
        return labels.getAll();
    }

    public Optional<LangString> getLabelForLanguageTag(String languageTag) {
        return labels.getLangStringForLanguageTag(languageTag, null, true);
    }

    public Optional<String> getLabelForLanguageTag(
            String language, String fallbackLanguage, boolean allowAnyIfNoMatch) {
        return labels.getStringForLanguageTag(language, fallbackLanguage, allowAnyIfNoMatch);
    }

    public boolean hasLabel(String label) {
        return labels.containsString(label);
    }

    public boolean isDeprecated() {
        return deprecated;
    }
}
