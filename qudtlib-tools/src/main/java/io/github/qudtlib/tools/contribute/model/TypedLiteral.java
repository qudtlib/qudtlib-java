package io.github.qudtlib.tools.contribute.model;

import org.eclipse.rdf4j.model.base.CoreDatatype;

public class TypedLiteral {
    private String literal;
    private String typeIRI;

    public TypedLiteral(String literal, String typeIRI) {
        this.literal = literal;
        this.typeIRI = typeIRI;
    }

    public TypedLiteral(String literal) {
        this.literal = literal;
        this.typeIRI = CoreDatatype.XSD.STRING.toString();
    }

    public String getLiteral() {
        return literal;
    }

    public String getTypeIRI() {
        return typeIRI;
    }
}
