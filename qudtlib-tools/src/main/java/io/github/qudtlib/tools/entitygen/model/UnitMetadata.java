package io.github.qudtlib.tools.entitygen.model;

import io.github.qudtlib.vocab.QUDT;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class UnitMetadata extends CommonEntityMetadata {
    protected TypedLiteral qudtUcumCode;
    protected String qudtIec61360Code;
    protected String qudtUneceCommonCode;
    protected IRI qudtOmUnit;

    public static Builder builder() {
        return new Builder();
    }

    public TypedLiteral getQudtUcumCode() {
        return qudtUcumCode;
    }

    public String getQudtIec61360Code() {
        return qudtIec61360Code;
    }

    public String getQudtUneceCommonCode() {
        return qudtUneceCommonCode;
    }

    public IRI getQudtOmUnit() {
        return qudtOmUnit;
    }

    public static class Builder extends CommonEntityMetadata.Builder<UnitMetadata> {
        public Builder() {
            super(new UnitMetadata());
        }

        public <T extends CommonEntityMetadata.Builder> T QudtUcumCode(TypedLiteral qudtUcumCode) {
            this.product.qudtUcumCode = qudtUcumCode;
            return (T) this;
        }

        public <T extends CommonEntityMetadata.Builder> T QudtUcumCode(String qudtUcumCode) {
            this.product.qudtUcumCode = new TypedLiteral(qudtUcumCode, QUDT.UCUMcs.toString());
            return (T) this;
        }

        public <T extends CommonEntityMetadata.Builder> T QudtIec61360Code(
                String qudtIec61360Code) {
            this.product.qudtIec61360Code = qudtIec61360Code;
            return (T) this;
        }

        public <T extends CommonEntityMetadata.Builder> T QudtUneceCommonCode(
                String qudtUneceCommonCode) {
            this.product.qudtUneceCommonCode = qudtUneceCommonCode;
            return (T) this;
        }

        public <T extends CommonEntityMetadata.Builder> T QudtOmUnit(String omUnitIri) {
            this.product.qudtOmUnit = SimpleValueFactory.getInstance().createIRI(omUnitIri);
            return (T) this;
        }

        public <T extends CommonEntityMetadata.Builder> T QudtOmUnit(IRI omUnitIri) {
            this.product.qudtOmUnit = omUnitIri;
            return (T) this;
        }
    }
}
