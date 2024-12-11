package io.github.qudtlib.support.parse;

import io.github.qudtlib.model.FactorUnit;
import java.util.Objects;

public class ParsedUnit {
    private final FactorUnit factorUnit;
    private final String token;

    public ParsedUnit(FactorUnit factorUnit, String token) {
        this.factorUnit = factorUnit;
        this.token = token;
    }

    public FactorUnit getFactorUnit() {
        return factorUnit;
    }

    public String getToken() {
        return token;
    }

    public ParsedUnit pow(int exponent, String token) {
        return new ParsedUnit(factorUnit.pow(exponent), this.token + token);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParsedUnit that = (ParsedUnit) o;
        return Objects.equals(factorUnit, that.factorUnit) && Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(factorUnit, token);
    }

    @Override
    public String toString() {
        return "ParsedUnit{" + "factorUnit=" + factorUnit + ", token='" + token + '\'' + '}';
    }
}
