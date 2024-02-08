package io.github.qudtlib.tools.contribute.support.tree;

import static io.github.qudtlib.tools.contribute.support.FormattingHelper.format;

import io.github.qudtlib.model.FactorUnit;
import io.github.qudtlib.model.Unit;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;
import java.util.function.Function;

public class UnitTree {

    public static String formatFactorUnitTree(Unit forUnit) {
        return formatFactorUnitTree(forUnit, null);
    }

    public static String formatFactorUnitTree(
            Unit forUnit, Function<FactorUnit, String> optionalFormatter) {
        StringBuilder sb = new StringBuilder();
        NodeVisitor formatter =
                new FormattingNodeVisitor<FactorUnit>(sb)
                        .nodeFormatDefault(
                                node -> {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    if (optionalFormatter != null) {
                                        stringBuilder.append(
                                                optionalFormatter.apply(node.getData()));
                                    } else {
                                        String asString =
                                                String.format("%-10s", node.getData().toString());
                                        stringBuilder
                                                .append(asString)
                                                .append(" ")
                                                .append(
                                                        node.getData()
                                                                .getDimensionVectorIri()
                                                                .orElse(
                                                                        "[missing dimension vector]"));
                                    }
                                    return stringBuilder.toString();
                                });
        Node<FactorUnit> root = buildFactorUnitTree(FactorUnit.ofUnit(forUnit));
        TreeWalker walker = new TreeWalker<FactorUnit>(root);
        walker.walkDepthFirst(formatter);
        return sb.toString();
    }

    public static Node<FactorUnit> buildFactorUnitTree(FactorUnit forUnit) {
        Node<FactorUnit> node = new Node<>(forUnit);
        Unit unitForRecursion = node.getData().getUnit();
        if (!unitForRecursion.hasFactorUnits()) {
            if (unitForRecursion.isScaled()) {
                unitForRecursion = unitForRecursion.getScalingOf().get();
            }
        }
        if (unitForRecursion.hasFactorUnits()) {
            for (FactorUnit fu : unitForRecursion.getFactorUnits().getFactorUnits()) {
                node.addChild(buildFactorUnitTree(fu));
            }
        }
        return node;
    }

    public static String makeFactorUnitTreeShowingConversionMultipliers(Unit unit) {
        Optional<BigDecimal> multiplierCalculated =
                unit.getFactorUnits().getConversionMultiplierOpt();
        String factorUnitTree =
                formatFactorUnitTree(
                        unit,
                        factorUnit -> {
                            StringBuilder sb =
                                    new StringBuilder()
                                            .append(factorUnit.getUnit().getIriAbbreviated())
                                            .append(
                                                    factorUnit.getExponent() == 1
                                                            ? ""
                                                            : "^" + factorUnit.getExponent())
                                            .append(" multiplier: ")
                                            .append(
                                                    factorUnit
                                                            .getUnit()
                                                            .getConversionMultiplier()
                                                            .map(m -> format(m))
                                                            .orElse("[no conversionMultiplier]"));
                            if (factorUnit.getUnit().equals(unit)
                                    && multiplierCalculated.isPresent()
                                    && unit.getConversionMultiplier()
                                            .map(
                                                    cm ->
                                                            cm.compareTo(multiplierCalculated.get())
                                                                    != 0)
                                            .orElse(true)) {
                                sb.append(
                                        String.format(
                                                " (correct: %s)",
                                                format(multiplierCalculated.get())));
                            }
                            if (factorUnit.getExponent() != 1) {
                                sb.append(" (multiplier")
                                        .append(
                                                factorUnit.getExponent() == 1
                                                        ? ""
                                                        : "^" + factorUnit.getExponent())
                                        .append(": ")
                                        .append(
                                                factorUnit
                                                        .getUnit()
                                                        .getConversionMultiplier()
                                                        .map(
                                                                m ->
                                                                        m.pow(
                                                                                factorUnit
                                                                                        .getExponent(),
                                                                                MathContext
                                                                                        .DECIMAL128))
                                                        .map(m -> format(m))
                                                        .orElse("[no conversionMultiplier]"))
                                        .append(")");
                            }
                            return sb.toString();
                        });
        return factorUnitTree;
    }

    public static String makeFactorUnitTreeShowingSymbols(Unit unit) {
        Optional<String> symbolCalculated = unit.getFactorUnits().getSymbol();
        String factorUnitTree =
                formatFactorUnitTree(
                        unit,
                        factorUnit -> {
                            StringBuilder sb =
                                    new StringBuilder()
                                            .append(factorUnit.getUnit().getIriAbbreviated())
                                            .append(
                                                    factorUnit.getExponent() == 1
                                                            ? ""
                                                            : "^" + factorUnit.getExponent())
                                            .append(" symbol: ")
                                            .append(
                                                    factorUnit
                                                            .getUnit()
                                                            .getSymbol()
                                                            .orElse("[no symbol]"));
                            if (factorUnit.getUnit().equals(unit)
                                    && symbolCalculated.isPresent()
                                    && unit.getSymbol()
                                            .map(s -> !s.equals(symbolCalculated.get()))
                                            .orElse(true)) {
                                sb.append(String.format(" (correct: %s)", symbolCalculated.get()));
                            }
                            return sb.toString();
                        });
        return factorUnitTree;
    }

    public static String makeFactorUnitTreeShowingUcumCodes(Unit unit) {
        Optional<String> ucumCodeCalculated = unit.getFactorUnits().getUcumCode();
        String factorUnitTree =
                formatFactorUnitTree(
                        unit,
                        factorUnit -> {
                            StringBuilder sb =
                                    new StringBuilder()
                                            .append(factorUnit.getUnit().getIriAbbreviated())
                                            .append(
                                                    factorUnit.getExponent() == 1
                                                            ? ""
                                                            : "^" + factorUnit.getExponent())
                                            .append(" ucumCode: ")
                                            .append(
                                                    factorUnit
                                                            .getUnit()
                                                            .getUcumCode()
                                                            .orElse("[no ucumCode]"));
                            if (factorUnit.getUnit().equals(unit)
                                    && ucumCodeCalculated.isPresent()
                                    && unit.getUcumCode()
                                            .map(s -> !s.equals(ucumCodeCalculated.get()))
                                            .orElse(true)) {
                                sb.append(
                                        String.format(" (correct: %s)", ucumCodeCalculated.get()));
                            }
                            return sb.toString();
                        });
        return factorUnitTree;
    }
}
