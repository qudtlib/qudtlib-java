package io.github.qudtlib.model;

import io.github.qudtlib.nodedef.Builder;
import io.github.qudtlib.nodedef.NodeDefinitionBase;
import io.github.qudtlib.nodedef.SelfSmuggler;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class PhysicalConstant extends SelfSmuggler {

    private final String iri;

    private final QuantityKind quantityKind;

    private final ConstantValue constantValue;

    private final LangStrings labels;

    private final boolean deprecated;

    public PhysicalConstant(Definition definition) {
        super(definition);
        this.iri = definition.iri;
        this.quantityKind = definition.quantityKindDef.build();
        this.constantValue = definition.constantValueDef.build();
        this.labels = new LangStrings(definition.labels);
        this.deprecated = definition.deprecated;
    }

    public static Definition definition(String iri) {
        return new Definition(iri);
    }

    public static Definition definition(PhysicalConstant product) {
        return new Definition(product);
    }

    public static class Definition extends NodeDefinitionBase<String, PhysicalConstant> {
        private String iri = null;

        private Builder<QuantityKind> quantityKindDef = null;

        private Builder<ConstantValue> constantValueDef = null;

        private Set<LangString> labels = new HashSet<>();

        private boolean deprecated = false;

        public Definition(String id, PhysicalConstant presetProduct) {
            super(id, presetProduct);
            this.iri = id;
        }

        public Definition(PhysicalConstant product) {
            this(product.getIri(), product);
        }

        public Definition(String id) {
            super(id);
            this.iri = id;
        }

        @Override
        protected PhysicalConstant doBuild() {
            return new PhysicalConstant(this);
        }

        public <T extends Definition> T iri(String iri) {
            this.iri = iri;
            return (T) this;
        }

        public <T extends Definition> T quantityKind(Builder<QuantityKind> quantityKindDef) {
            this.quantityKindDef = quantityKindDef;
            return (T) this;
        }

        public <T extends Definition> T quantityKind(QuantityKind quantityKind) {
            this.quantityKindDef = QuantityKind.definition(quantityKind);
            return (T) this;
        }

        public <T extends Definition> T constantValue(Builder<ConstantValue> constantValueDef) {
            this.constantValueDef = constantValueDef;
            return (T) this;
        }

        public <T extends Definition> T constantValue(ConstantValue constantValue) {
            this.constantValueDef = ConstantValue.definition(constantValue);
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
            this.deprecated = deprecated;
            return (T) this;
        }
    }

    public String getIri() {
        return iri;
    }

    public QuantityKind getQuantityKind() {
        return quantityKind;
    }

    public ConstantValue getConstantValue() {
        return constantValue;
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
