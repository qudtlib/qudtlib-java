package io.github.qudtlib.constgen;

import java.util.Objects;
import java.util.Optional;

/**
 * Class representing constant names/labels/local names in IRIs for generating RDF vocabularies.
 *
 * @author Florian Kleedorfer
 * @since 1.0
 */
public class Constant {
    private final String codeConstantName;
    private final String iriLocalname;
    private final String label;

    private final String iri;

    private final String typeName;

    private final String symbol;
    private final String valueFactory;

    public Constant(
            String codeConstantName,
            String iriLocalname,
            String label,
            String iri,
            String typeName,
            String symbol,
            String valueFactory) {
        Objects.requireNonNull(codeConstantName);
        Objects.requireNonNull(iriLocalname);
        Objects.requireNonNull(iri);
        Objects.requireNonNull(label);
        Objects.requireNonNull(typeName);
        Objects.requireNonNull(valueFactory);
        this.codeConstantName = codeConstantName;
        this.iriLocalname = iriLocalname;
        this.label = label;
        this.iri = iri;
        this.typeName = typeName;
        this.symbol = symbol;
        this.valueFactory = valueFactory;
    }

    public String getCodeConstantName() {
        return codeConstantName;
    }

    public String getIriLocalname() {
        return iriLocalname;
    }

    public String getLabel() {
        return label;
    }

    public String getIri() {
        return iri;
    }

    public String getTypeName() {
        return typeName;
    }

    public Optional<String> getSymbol() {
        return Optional.ofNullable(symbol);
    }

    public String getValueFactory() {
        return valueFactory;
    }
}
