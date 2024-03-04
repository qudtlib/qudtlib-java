package io.github.qudtlib.tools.contributions;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.QudtNamespaces;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;
import io.github.qudtlib.vocab.QUDT;
import java.math.BigDecimal;
import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;

public class OuputNewNumericValues {

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
                                                    addedStatements);
                                        }
                                        val = unit.getConversionOffset();
                                        if (val.isPresent()) {
                                            addNewNumericProperty(
                                                    unit.getIri(),
                                                    val.get(),
                                                    QUDT.conversionOffset,
                                                    QUDT.conversionOffsetSN,
                                                    addedStatements);
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
                                                addedStatements);
                                        Optional<BigDecimal> val =
                                                constantValue.getStandardUncertainty();
                                        if (val.isPresent()) {
                                            addNewNumericProperty(
                                                    constantValue.getIri(),
                                                    val.get(),
                                                    QUDT.standardUncertainty,
                                                    QUDT.standardUncertaintySN,
                                                    addedStatements);
                                        }
                                    });
                    tool.writeOut(addedStatements, System.out);
                });
    }

    private static void addNewNumericProperty(
            String iri, BigDecimal value, IRI property, IRI propertySN, Model addedStatements) {
        ValueFactory vf = SimpleValueFactory.getInstance();
        addedStatements.add(vf.createIRI(iri), property, vf.createLiteral(value));
        addedStatements.add(vf.createIRI(iri), propertySN, createDoubleLiteral(value));
    }

    private static Value createDoubleLiteral(BigDecimal val) {
        ValueFactory vf = SimpleValueFactory.getInstance();
        return vf.createLiteral(val.toString(), CoreDatatype.XSD.DOUBLE);
    }
}
