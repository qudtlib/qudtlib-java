package io.github.qudtlib.constgen;

/**
 * Class representing constant names/labels/local names in IRIs for generating RDF vocabularies.
 *
 * @author Florian Kleedorfer
 * @since 1.0
 */
public class Constant {
    private final String javaName;
    private final String localName;
    private final String label;

    public Constant(String javaName, String localName, String label) {
        this.javaName = javaName;
        this.localName = localName;
        this.label = label;
    }

    public String getJavaName() {
        return javaName;
    }

    public String getLocalName() {
        return localName;
    }

    public String getLabel() {
        return label;
    }
}
