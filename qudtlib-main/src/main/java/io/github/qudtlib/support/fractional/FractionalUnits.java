package io.github.qudtlib.support.fractional;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.exception.NotFoundException;
import io.github.qudtlib.model.FactorUnit;
import io.github.qudtlib.model.Unit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FractionalUnits {

    public static FractionalDimensionVector getFractionalDimensionVector(Unit unit) {
        String[] unitNameConstituents = getIriLocalname(unit.getIri()).split("-");
        List<FactorUnit> numerator = new ArrayList<>();
        List<FactorUnit> denominator = new ArrayList<>();
        List<FactorUnit> currentFactors = numerator;
        for (String currentUnitNameConstituent : unitNameConstituents) {
            FactorUnit found = null;
            if (currentUnitNameConstituent.equals("PER")) {
                currentFactors = denominator;
                continue;
            }
            Pattern p = Pattern.compile("(.+)(-?\\d)?");
            Matcher m = p.matcher(currentUnitNameConstituent);
            if (!m.find()) {
                throw new RuntimeException(
                        String.format(
                                "Name particle %s of unit %s does not seem to be a factor unit",
                                currentUnitNameConstituent, unit.getIri()));
            }
            String unitName = m.group(1);
            Unit currentFactorUnit = null;
            try {
                currentFactorUnit = Qudt.unitFromLocalnameRequired(unitName);
            } catch (NotFoundException e) {
                try {
                    currentFactorUnit = Qudt.currencyFromLocalnameRequired(unitName);
                } catch (NotFoundException e2) {
                    throw new RuntimeException(
                            String.format(
                                    "Name particle %s of unit %s is not a QUDT unit or currency",
                                    currentUnitNameConstituent, unit.getIri()),
                            e2);
                }
            }
            String exponentStr = m.group(2);
            int currentExponent = 1;
            if (exponentStr != null) {
                currentExponent = Integer.valueOf(exponentStr);
            }
            currentExponent = Math.abs(currentExponent);
            currentFactors.add(new FactorUnit(currentFactorUnit, currentExponent));
        }
        return new FractionalDimensionVector(numerator, denominator);
    }

    private static String getIriLocalname(String iri) {
        if (iri == null) {
            return null;
        }
        for (int i = iri.length(); i >= 0; i--) {
            Character ch = iri.charAt(i - 1);
            if (ch == '/' || ch == '#') {
                return iri.substring(i);
            }
        }
        return "";
    }
}
