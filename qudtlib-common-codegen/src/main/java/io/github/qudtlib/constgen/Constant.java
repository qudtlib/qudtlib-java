package io.github.qudtlib.constgen;

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

    public Constant(String codeConstantName, String iriLocalname, String label) {
        this.codeConstantName = codeConstantName;
        this.iriLocalname = iriLocalname;
        this.label = label;
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
}
