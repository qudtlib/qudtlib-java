package io.github.qudtlib.tools.contributions;

import static io.github.qudtlib.model.Units.UNITLESS;
import static java.util.stream.Collectors.joining;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.*;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;
import io.github.qudtlib.tools.contribute.support.IndentedOutputStream;
import io.github.qudtlib.tools.contribute.support.SelectionHelper;
import io.github.qudtlib.tools.contribute.support.tree.Node;
import io.github.qudtlib.tools.contribute.support.tree.QuantityKindTree;
import io.github.qudtlib.vocab.QUDT;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;

public class CheckDimensionVectors {
    public static void main(String[] args) {
        QudtEntityGenerator entityGenerator = new QudtEntityGenerator();
        Map<Unit, String> newDimVectors = new HashMap<>();
        entityGenerator.unitOfWork(
                tool -> {
                    Qudt.allUnits().stream()
                            .sorted(Comparator.comparing(Unit::getIri))
                            .filter(u -> !u.isDeprecated())
                            .filter(u -> !u.equals(Units.UNKNOWN) && !u.equals(UNITLESS))
                            .forEach(
                                    u -> {
                                        String dimVector = null;
                                        FactorUnits factorUnits = null;
                                        List<FactorUnit> factorUnitList =
                                                u.getFactorUnits().getFactorUnits();
                                        factorUnits =
                                                new FactorUnits(
                                                        FactorUnits.sortAccordingToUnitLocalname(
                                                                u.getIriLocalname(),
                                                                factorUnitList));
                                        try {
                                            dimVector = factorUnits.getDimensionVectorIri();
                                            final String finalDimVector = dimVector;
                                            if (dimVector != null) {
                                                if ((u.getDimensionVectorIri().isEmpty()
                                                        || !u.getDimensionVectorIri()
                                                                .get()
                                                                .equals(dimVector))) {
                                                    if (u.getDimensionVectorIri().isPresent()) {
                                                        System.err.println(
                                                                String.format(
                                                                        "wrong dimension vector on %s %s will be replaced by %s",
                                                                        u.getIriAbbreviated(),
                                                                        u.getDimensionVectorIri()
                                                                                .get(),
                                                                        dimVector));
                                                    } else {
                                                        System.err.println(
                                                                String.format(
                                                                        "missing dimension vector on %s will be set to %s",
                                                                        u.getIriAbbreviated(),
                                                                        dimVector));
                                                    }
                                                    newDimVectors.put(u, dimVector);
                                                }
                                            } else {
                                                System.err.println(
                                                        String.format(
                                                                "Cannot check/fix dim vector for %s: one of the constituent units has no dim vector",
                                                                u.getIri()));
                                            }
                                        } catch (Exception e) {
                                            System.err.println(
                                                    String.format(
                                                            "Cannot add symbol for %s: %s",
                                                            u.getIri(), e.getMessage()));
                                        }
                                    });
                });
        Model addedStatements = new TreeModel();
        newDimVectors.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getIri()))
                .forEach(
                        e -> {
                            entityGenerator.unitOfWork(
                                    tool -> {
                                        PrintStream out =
                                                new PrintStream(
                                                        new IndentedOutputStream(
                                                                System.out, "  # "));
                                        Unit unit = e.getKey();
                                        String correctedDimensionVector = e.getValue();
                                        out.println(
                                                String.format(
                                                        "%s has incorrect dimension vector:\n\t%s, which should be \n\t%s",
                                                        unit.getIriAbbreviated(),
                                                        unit.getDimensionVectorIri()
                                                                .orElse("[no dim vector]"),
                                                        correctedDimensionVector));
                                        tool.printFactorUnitTree(
                                                unit,
                                                u ->
                                                        String.format(
                                                                "%-30s\t%s",
                                                                (u.getUnit().getIriAbbreviated()
                                                                        + (u.getExponent() == 1
                                                                                ? ""
                                                                                : "^"
                                                                                        + u
                                                                                                .getExponent())),
                                                                u.getDimensionVectorIri()
                                                                        .orElse("[no dim vector]")),
                                                out);

                                        Set<QuantityKind> quantityKindSet = unit.getQuantityKinds();
                                        out.println(
                                                String.format(
                                                        "the unit has these quantityKinds: %s",
                                                        quantityKindSet.stream()
                                                                .map(QuantityKind::getIriLocalname)
                                                                .collect(
                                                                        Collectors.joining(", "))));

                                        if (quantityKindSet.equals(Set.of(QuantityKinds.Unknown))) {
                                            out.println(" (omitting unit/quantity kind tree)");
                                        } else {
                                            StringBuilder stringBuilder = new StringBuilder();
                                            QuantityKindTree.makeAndFormatQuantityKindAndUnitTree(
                                                    quantityKindSet, stringBuilder);
                                            out.print(stringBuilder.toString());
                                        }
                                        Set<QuantityKind> fittingQuantityKinds =
                                                SelectionHelper.getQuantityKindsByDimensionVector(
                                                        correctedDimensionVector);
                                        out.println(
                                                String.format(
                                                        "quantity kinds that fit the corrected dimension vector %s, and associated units:",
                                                        correctedDimensionVector));

                                        if (fittingQuantityKinds.isEmpty()) {
                                            out.println("(none found)");
                                        } else {
                                            StringBuilder stringBuilder = new StringBuilder();
                                            QuantityKindTree.makeAndFormatQuantityKindAndUnitTree(
                                                    fittingQuantityKinds, stringBuilder);
                                            out.print(stringBuilder.toString());
                                        }
                                        System.out.println(
                                                String.format(
                                                        "%s qudt:hasDimensionVector %s .",
                                                        unit.getIriAbbreviated(),
                                                        QudtNamespaces.dimensionVector.abbreviate(
                                                                correctedDimensionVector)));
                                        ValueFactory vf = SimpleValueFactory.getInstance();
                                        addedStatements.add(
                                                vf.createIRI(unit.getIri()),
                                                QUDT.hasDimensionVector,
                                                vf.createIRI(correctedDimensionVector));
                                        System.out.println();
                                    });
                        });
        System.out.println("\n\nSTATEMENTS TO DELETE");
        System.out.println("PREFIX qudt: <http://qudt.org/schema/qudt/>");
        System.out.println("DELETE { ?u qudt:hasDimensionVector ?dv } ");
        System.out.println("WHERE { ?u qudt:hasDimensionVector ?dv .");
        System.out.println("VALUES  ?u {");
        System.out.println(
                addedStatements.stream()
                        .map(s -> s.getSubject())
                        .filter(s -> s.isIRI())
                        .map(s -> "<" + ((IRI) s).toString() + ">")
                        .collect(joining("\n\t")));
        System.out.println("}}");
    }

    private static String getUnitOrQuantityKindIri(Node<?> node) {
        return (node.getData() instanceof Unit)
                ? ((Unit) node.getData()).getIriAbbreviated()
                : QudtNamespaces.quantityKind.abbreviate(((QuantityKind) node.getData()).getIri());
    }

    public static List<Unit> getUnitsByDimensionVector(String dimensionVectorIri) {
        return Qudt.allUnits().stream()
                .filter(
                        u ->
                                u.getDimensionVectorIri()
                                        .map(dv -> dv.equals(dimensionVectorIri))
                                        .orElse(false))
                .collect(Collectors.toList());
    }
}
