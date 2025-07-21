package io.github.qudtlib.tools.contributions;

import static java.util.stream.Collectors.joining;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.Namespace;
import io.github.qudtlib.model.QudtNamespaces;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;
import io.github.qudtlib.vocab.QUDT;
import java.math.BigDecimal;
import java.util.*;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;

public class GenerateNumericValuesAsXsdDoubleAndXsdDecimal {

    private static Map<IRI, Collection<String>> unitsByReplacedPredicate = new HashMap<>();

    public static void main(String[] args) {
        QudtEntityGenerator generator = new QudtEntityGenerator();
        Model addedStatements = new TreeModel();
        ValueFactory vf = SimpleValueFactory.getInstance();
        generator.unitOfWork(
                tool -> {
                    Qudt.allUnits().stream()
                            .forEach(
                                    unit -> {
                                        Optional<BigDecimal> val = unit.getConversionMultiplier();
                                        if (val.isPresent()) {
                                            addNewNumericProperty(
                                                    unit.getIri(),
                                                    val.get(),
                                                    QUDT.conversionMultiplier,
                                                    QUDT.conversionMultiplierSN,
                                                    addedStatements,
                                                    unitsByReplacedPredicate);
                                        }
                                        val = unit.getConversionOffset();
                                        if (val.isPresent()) {
                                            addNewNumericProperty(
                                                    unit.getIri(),
                                                    val.get(),
                                                    QUDT.conversionOffset,
                                                    QUDT.conversionOffsetSN,
                                                    addedStatements,
                                                    unitsByReplacedPredicate);
                                        }
                                    });
                    tool.writeOut(addedStatements, System.out);
                    System.out.println("\n\n\n\n\n");
                    addedStatements.clear();
                });
        generator.unitOfWork(
                tool -> {
                    addedStatements.setNamespace(
                            QudtNamespaces.constant.getBaseIri(),
                            QudtNamespaces.constant.getAbbreviationPrefix());
                    Qudt.allConstantValues().stream()
                            .forEach(
                                    constantValue -> {
                                        addNewNumericProperty(
                                                constantValue.getIri(),
                                                constantValue.getValue(),
                                                QUDT.value,
                                                QUDT.valueSN,
                                                addedStatements,
                                                unitsByReplacedPredicate);
                                        Optional<BigDecimal> val =
                                                constantValue.getStandardUncertainty();
                                        if (val.isPresent()) {
                                            addNewNumericProperty(
                                                    constantValue.getIri(),
                                                    val.get(),
                                                    QUDT.standardUncertainty,
                                                    QUDT.standardUncertaintySN,
                                                    addedStatements,
                                                    unitsByReplacedPredicate);
                                        }
                                    });
                    tool.writeOut(addedStatements, System.out);
                });

        printDeleteQuery(unitsByReplacedPredicate);
    }

    private static void addNewNumericProperty(
            String iri,
            BigDecimal value,
            IRI property,
            IRI propertySN,
            Model addedStatements,
            Map<IRI, Collection<String>> unitsByReplacedPredicate) {
        ValueFactory vf = SimpleValueFactory.getInstance();
        Collection<String> units = unitsByReplacedPredicate.get(property);
        if (units == null) {
            units = new HashSet<>();
            unitsByReplacedPredicate.put(property, units);
        }
        units.add(iri);
        addedStatements.add(vf.createIRI(iri), property, vf.createLiteral(value));
        addedStatements.add(vf.createIRI(iri), propertySN, createDoubleLiteral(value));
    }

    private static Value createDoubleLiteral(BigDecimal val) {
        ValueFactory vf = SimpleValueFactory.getInstance();
        return vf.createLiteral(val.toString(), CoreDatatype.XSD.DOUBLE);
    }

    private static void printDeleteQuery(Map<IRI, Collection<String>> unitsByReplacedPredicate) {
        for (Map.Entry<IRI, Collection<String>> entry : unitsByReplacedPredicate.entrySet()) {
            IRI predicate = entry.getKey();
            Collection<String> subjects = entry.getValue();
            System.out.println("\n\n\nSTATEMENTS TO DELETE:\n");
            System.out.println("PREFIX qudt: <http://qudt.org/schema/qudt/>");
            System.out.println("PREFIX unit: <http://qudt.org/vocab/unit/>");
            System.out.format(
                    "PREFIX %s: <%s>\n",
                    Qudt.NAMESPACES.constant.getAbbreviationPrefix(),
                    Qudt.NAMESPACES.constant.getBaseIri());
            System.out.format("DELETE { ?u %s ?m } \n", predicate);
            System.out.format("WHERE { ?u %s ?m .\n", predicate);
            System.out.println("\tVALUES  ?u {");
            System.out.println(
                    subjects.stream()
                            .map(iri -> abbreviateIfPossible(iri))
                            .sorted()
                            .collect(joining("\n\t\t", "\n\t\t", "")));
            System.out.println("\t}\n}\n\n\n");
        }
    }

    private static String abbreviateIfPossible(String iri) {
        String abbrev = abbreviateIfPossible(iri, Qudt.NAMESPACES.unit);
        if (abbrev != null) {
            return abbrev;
        }
        abbrev = abbreviateIfPossible(iri, Qudt.NAMESPACES.constant);
        if (abbrev != null) {
            return abbrev;
        }
        return iri;
    }

    private static String abbreviateIfPossible(String iri, Namespace namespace) {
        if (namespace.isFullNamespaceIri(iri)) {
            return namespace.abbreviate(iri);
        }
        return null;
    }
}
