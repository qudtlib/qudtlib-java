package io.github.qudtlib.tools.contribute.model;

import io.github.qudtlib.vocab.QUDT;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class CommonEntityMetadata {

    /**
     * E.g. dcterms:description "\"Revolution per Hour\" is a unit for 'Angular Velocity' expressed
     * as \\(rev/h\\)."^^qudt:LatexString ;
     */
    protected TypedLiteral dcTermsDescription;

    protected String plainTextDescription;

    /** qudt:expression "\\(rev/h\\)"^^qudt:LatexString ; */
    protected TypedLiteral qudtExpression;

    protected IRI rdfsIsDefinedBy;

    protected Set<TypedLiteral> qudtInformativeReference = new HashSet<>();

    protected TypedLiteral qudtIsoNormativeReference;
    protected IRI rdfsSeeAlso;

    protected TypedLiteral qudtDbpediaMatch;

    protected TypedLiteral latexDefinition;

    protected TypedLiteral latexSymbol;

    public abstract static class Builder<T extends CommonEntityMetadata>
            extends MetadataBuilder<T> {
        public Builder(T product) {
            super(product);
        }

        public <T extends Builder> T dcTermsDescription(TypedLiteral dcTermsDescription) {
            this.product.dcTermsDescription = dcTermsDescription;
            return (T) this;
        }

        public <T extends Builder> T dcTermsDescription(String dcTermsDescriptionHtml) {
            this.product.dcTermsDescription =
                    new TypedLiteral(
                            dcTermsDescriptionHtml, CoreDatatype.RDF.HTML.getIri().toString());
            return (T) this;
        }

        public <T extends Builder> T plainTextDescription(String plainTextDescription) {
            this.product.plainTextDescription = plainTextDescription;
            return (T) this;
        }

        public <T extends Builder> T qudtExpression(TypedLiteral qudtExpression) {
            this.product.qudtExpression = qudtExpression;
            return (T) this;
        }

        public <T extends Builder> T qudtExpression(String qudtExpressionLatex) {
            this.product.qudtExpression =
                    new TypedLiteral(qudtExpressionLatex, QUDT.LatexString.toString());
            return (T) this;
        }

        public <T extends Builder> T latexDefinition(TypedLiteral latexDefinition) {
            this.product.latexDefinition = latexDefinition;
            return (T) this;
        }

        public <T extends Builder> T latexDefinition(String latexDefinitionLatex) {
            this.product.latexDefinition =
                    new TypedLiteral(latexDefinitionLatex, QUDT.LatexString.toString());
            return (T) this;
        }

        public <T extends Builder> T latexSymbol(TypedLiteral latexSymbol) {
            this.product.latexSymbol = latexSymbol;
            return (T) this;
        }

        public <T extends Builder> T latexSymbol(String latexSymbolLatex) {
            this.product.latexSymbol =
                    new TypedLiteral(latexSymbolLatex, QUDT.LatexString.toString());
            return (T) this;
        }

        public <T extends Builder> T rdfsIsDefinedBy(String rdfsIsDefinedBy) {
            this.product.rdfsIsDefinedBy =
                    SimpleValueFactory.getInstance().createIRI(rdfsIsDefinedBy);
            return (T) this;
        }

        public <T extends Builder> T rdfsIsDefinedBy(IRI rdfsIsDefinedBy) {
            this.product.rdfsIsDefinedBy = rdfsIsDefinedBy;
            return (T) this;
        }

        public <T extends Builder> T qudtInformativeReference(
                TypedLiteral qudtInformativeReference) {
            this.product.qudtInformativeReference.add(qudtInformativeReference);
            return (T) this;
        }

        public <T extends Builder> T qudtInformativeReference(String informativeRefernceXsdAnyURI) {
            this.product.qudtInformativeReference.add(
                    new TypedLiteral(
                            informativeRefernceXsdAnyURI, CoreDatatype.XSD.ANYURI.toString()));
            return (T) this;
        }

        public <T extends Builder> T qudtIsoNormativeReference(
                TypedLiteral qudtIsoNormativeReference) {
            this.product.qudtIsoNormativeReference = qudtIsoNormativeReference;
            return (T) this;
        }

        public <T extends Builder> T qudtIsoNormativeReference(
                String isoNormativeRefernceXsdAnyURI) {
            this.product.qudtIsoNormativeReference =
                    new TypedLiteral(
                            isoNormativeRefernceXsdAnyURI, CoreDatatype.XSD.ANYURI.toString());
            return (T) this;
        }

        public <T extends Builder> T qudtDbpediaMatch(TypedLiteral qudtInformativeReference) {
            this.product.qudtDbpediaMatch = qudtInformativeReference;
            return (T) this;
        }

        public <T extends Builder> T qudtDbpediaMatch(String informativeRefernceXsdAnyURI) {
            this.product.qudtDbpediaMatch =
                    new TypedLiteral(
                            informativeRefernceXsdAnyURI, CoreDatatype.XSD.ANYURI.toString());
            return (T) this;
        }

        public <T extends Builder> T rdfsSeeAlso(String rdfsSeeAlso) {
            this.product.rdfsSeeAlso = SimpleValueFactory.getInstance().createIRI(rdfsSeeAlso);
            return (T) this;
        }

        public <T extends Builder> T rdfsSeeAlso(IRI rdfsSeeAlso) {
            this.product.rdfsSeeAlso = rdfsSeeAlso;
            return (T) this;
        }
    }

    public TypedLiteral getDcTermsDescription() {
        return dcTermsDescription;
    }

    public String getPlainTextDescription() {
        return plainTextDescription;
    }

    public TypedLiteral getQudtExpression() {
        return qudtExpression;
    }

    public IRI getRdfsIsDefinedBy() {
        return rdfsIsDefinedBy;
    }

    public Set<TypedLiteral> getQudtInformativeReference() {
        return qudtInformativeReference;
    }

    public IRI getRdfsSeeAlso() {
        return rdfsSeeAlso;
    }

    public TypedLiteral getQudtIsoNormativeReference() {
        return qudtIsoNormativeReference;
    }

    public TypedLiteral getQudtDbpediaMatch() {
        return qudtDbpediaMatch;
    }

    public TypedLiteral getLatexDefinition() {
        return latexDefinition;
    }

    public TypedLiteral getLatexSymbol() {
        return latexSymbol;
    }
}
