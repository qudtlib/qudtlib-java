package io.github.qudtlib.model;

public class QudtNamespaces {
    public static final Namespace quantityKind =
            new Namespace("http://qudt.org/vocab/quantitykind/", "qk");
    public static final Namespace unit = new Namespace("http://qudt.org/vocab/unit/", "unit");
    public static final Namespace currency =
            new Namespace("http://qudt.org/vocab/currency/", "cur");
    public static final Namespace prefix = new Namespace("http://qudt.org/vocab/prefix/", "prefix");
    public static final Namespace systemOfUnits =
            new Namespace("http://qudt.org/vocab/sou/", "sou");
    public static final Namespace qudt = new Namespace("http://qudt.org/schema/qudt/", "qudt");
    public static final Namespace dimensionVector =
            new Namespace("http://qudt.org/vocab/dimensionvector/", "qkdv");
    public static final Namespace constant =
            new Namespace("http://qudt.org/vocab/constant/", "constant");
}
