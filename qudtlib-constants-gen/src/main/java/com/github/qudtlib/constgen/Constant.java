package com.github.qudtlib.constgen;

public class Constant {
    private String javaName;
    private String localName;
    private String label;

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
