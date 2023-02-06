package io.github.qudtlib.model;
/** Encapsulates an iri and the prefix that is used to abbreviate it. */
public class Namespace {
    private final String abbreviationPrefix;
    private final String baseIri;

    /**
     * Constructs a namespace instace with specified baseIri and abbreviation prefix. For example,
     *
     * <pre>
     *     Namespace rdf = Namespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
     * </pre>
     *
     * will provide abbreviation/expansion of IRIs in the RDF namespace.
     *
     * @param baseIri the base IRI of the namespece, e.g.
     *     "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     * @param abbreviationPrefix the abbreviation prefix to use, e.g. "rdf"
     */
    public Namespace(String baseIri, String abbreviationPrefix) {
        this.abbreviationPrefix = abbreviationPrefix;
        this.baseIri = baseIri;
    }

    /** @return the string used to abbreviate IRIs in this namespace. */
    public String getAbbreviationPrefix() {
        return abbreviationPrefix;
    }

    /** @return the base IRI of the namespace. */
    public String getBaseIri() {
        return baseIri;
    }

    /**
     * @param iri the IRI to abbreviate
     * @return an abbreviated IRI if the specified iri starts with the baseIri; the unchanged input
     *     String otherwise;
     */
    public String abbreviate(String iri) {
        if (isFullNamespaceIri(iri)) {
            return this.abbreviationPrefix + ":" + iri.substring(this.baseIri.length());
        }
        return new String(iri);
    }

    /**
     * Expands an IRI from this namespace that is abbreviated.
     *
     * @param abbreviatedIri the IRI to expand
     * @return the expanded IRI
     */
    public String expand(String abbreviatedIri) {
        if (isAbbreviatedNamespaceIri(abbreviatedIri)) {
            return this.baseIri + abbreviatedIri.substring(this.abbreviationPrefix.length() + 1);
        }
        return new String(abbreviatedIri);
    }

    /**
     * @param abbreviatedIri the IRI to check
     * @return true if the specified abbreviatedIri starts with the namespace's abbreviation prefix.
     */
    public boolean isAbbreviatedNamespaceIri(String abbreviatedIri) {
        return abbreviatedIri.startsWith(this.abbreviationPrefix + ':');
    }

    /**
     * @param iri the IRI to check
     * @return true if the specified iri starts with the namespace's baseIri.
     */
    public boolean isFullNamespaceIri(String iri) {
        return iri.startsWith(this.baseIri);
    }

    /**
     * Prepends the namespace's baseIri to the specified localName.
     *
     * @param localName the last part of the iri to be created
     * @return the IRI created in this namespace
     */
    public String makeIriInNamespace(String localName) {
        return this.baseIri + localName;
    }
}
