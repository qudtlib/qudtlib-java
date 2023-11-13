package io.github.qudtlib.init;

import io.github.qudtlib.exception.NotFoundException;
import io.github.qudtlib.exception.QudtInitializationException;
import io.github.qudtlib.model.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes the QUDTLib model based on pre-processed QUDT TTL files.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class InitializerImpl implements Initializer {
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Repository qudtRepository;
    private final String queryLoadUnit;
    private final String queryLoadQuantityKind;
    private final String queryLoadPrefix;
    private final String queryLoadFactorUnits;
    private final String queryLoadSystemsOfUnits;

    public InitializerImpl() {
        Model m = loadQudtModel();
        qudtRepository = new SailRepository(new MemoryStore());
        try (RepositoryConnection con = qudtRepository.getConnection()) {
            con.add(m);
            con.commit();
        }
        queryLoadUnit = loadQuery("qudtlib/query/unit.rq");
        queryLoadQuantityKind = loadQuery("qudtlib/query/quantitykind.rq");
        queryLoadPrefix = loadQuery("qudtlib/query/prefix.rq");
        queryLoadFactorUnits = loadQuery("qudtlib/query/factor-units.rq");
        queryLoadSystemsOfUnits = loadQuery("qudtlib/query/system-of-units.rq");
    }

    @Override
    public Definitions loadData() {
        Definitions definitions = new Definitions();
        try (RepositoryConnection con = qudtRepository.getConnection()) {
            populateUnitDefinitions(con, definitions);
            populateQuantityKindDefinitions(con, definitions);
            populatePrefixDefinitions(con, definitions);
            populateFactorUnits(con, definitions);
            populateSystemOfUnitsDefinitions(con, definitions);
        }
        return definitions;
    }

    private void populateUnitDefinitions(RepositoryConnection con, Definitions definitions) {
        TupleQuery query = con.prepareTupleQuery(queryLoadUnit);
        try (TupleQueryResult result = query.evaluate()) {
            Iterator<BindingSet> solutions = result.iterator();
            Unit.Definition unitDefinition = null;
            while (solutions.hasNext()) {
                BindingSet bs = solutions.next();
                String currentUnitIri = bs.getValue("unit").stringValue();
                if (unitDefinition != null && !currentUnitIri.equals(unitDefinition.getId())) {
                    definitions.addUnitDefinition(unitDefinition);
                    unitDefinition = null;
                }
                if (unitDefinition == null) {
                    unitDefinition = makeUnitBuilder(bs, definitions);
                }
                unitDefinition
                        .addLabel(
                                getIfPresent(
                                        bs,
                                        "label",
                                        v ->
                                                new LangString(
                                                        ((Literal) v).getLabel(),
                                                        ((Literal) v).getLanguage().orElse(null))))
                        .addQuantityKind(
                                getIfPresent(
                                        bs,
                                        "quantityKind",
                                        compose(
                                                Value::stringValue,
                                                definitions::expectQuantityKindDefinition)))
                        .addSystemOfUnits(
                                getIfPresent(
                                        bs,
                                        "systemOfUnits",
                                        compose(
                                                Value::stringValue,
                                                definitions::expectSystemOfUnitsDefinition)))
                        .addExactMatch(
                                getIfPresent(
                                        bs,
                                        "exactMatch",
                                        compose(
                                                Value::stringValue,
                                                definitions::expectUnitDefinition)));
            }
            if (unitDefinition != null) {
                definitions.addUnitDefinition(unitDefinition);
            }
            if (!definitions.hasUnitDefinitions()) {
                throw new NotFoundException(
                        "No units found for unit query with bindings "
                                + bindingsToString(query.getBindings()));
            }
        }
    }

    private String bindingsToString(BindingSet bindingSet) {
        StringBuilder sb = new StringBuilder();
        return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(
                                bindingSet.iterator(), Spliterator.IMMUTABLE),
                        false)
                .map(b -> b.getName() + "->" + b.getValue())
                .collect(Collectors.joining(", ", "[ ", " ]"));
    }

    private void populateQuantityKindDefinitions(
            RepositoryConnection con, Definitions definitions) {
        TupleQuery query = con.prepareTupleQuery(queryLoadQuantityKind);
        try (TupleQueryResult result = query.evaluate()) {
            Iterator<BindingSet> solutions = result.iterator();
            QuantityKind.Definition quantityKindDefinition = null;
            String builderIri = null;
            while (solutions.hasNext()) {
                BindingSet bs = solutions.next();
                String currentQuantityKindIri = bs.getValue("quantityKind").stringValue();
                if (quantityKindDefinition != null && !currentQuantityKindIri.equals(builderIri)) {
                    definitions.addQuantityKindDefinition(quantityKindDefinition);
                    quantityKindDefinition = null;
                }
                if (quantityKindDefinition == null) {
                    quantityKindDefinition = makeQuantityKindBuilder(bs);
                    builderIri = bs.getValue("quantityKind").stringValue();
                }
                if (bs.hasBinding("label")) {
                    Value val = bs.getValue("label");
                    Literal lit = (Literal) val;
                    quantityKindDefinition.addLabel(
                            new LangString(lit.getLabel(), lit.getLanguage().orElse(null)));
                }
                if (bs.hasBinding("broaderQuantityKind")) {
                    String broaderQkIri = bs.getValue("broaderQuantityKind").stringValue();
                    quantityKindDefinition.addBroaderQuantityKind(
                            definitions.expectQuantityKindDefinition(broaderQkIri));
                }
                if (bs.hasBinding("applicableUnit")) {
                    quantityKindDefinition.addApplicableUnit(
                            definitions.expectUnitDefinition(
                                    bs.getValue("applicableUnit").stringValue()));
                }
                if (bs.hasBinding("exactMatch")) {
                    quantityKindDefinition.addExactMatch(
                            definitions.expectQuantityKindDefinition(
                                    bs.getValue("exactMatch").stringValue()));
                }
            }
            if (quantityKindDefinition != null) {
                definitions.addQuantityKindDefinition(quantityKindDefinition);
            }
        }
    }

    private void populateSystemOfUnitsDefinitions(
            RepositoryConnection con, Definitions definitions) {
        TupleQuery query = con.prepareTupleQuery(queryLoadSystemsOfUnits);
        try (TupleQueryResult result = query.evaluate()) {
            Iterator<BindingSet> solutions = result.iterator();
            SystemOfUnits.Definition systemOfUnitsDefinition = null;
            while (solutions.hasNext()) {
                BindingSet bs = solutions.next();
                String currentSystemOfUnitsIri = bs.getValue("systemOfUnits").stringValue();
                if (systemOfUnitsDefinition != null
                        && !currentSystemOfUnitsIri.equals(systemOfUnitsDefinition.getId())) {
                    definitions.addSystemOfUnitsDefinition(systemOfUnitsDefinition);
                    systemOfUnitsDefinition = null;
                }
                if (systemOfUnitsDefinition == null) {
                    systemOfUnitsDefinition = makeSystemOfUnitsBuilder(bs);
                }
                systemOfUnitsDefinition
                        .addLabel(
                                getIfPresent(
                                        bs,
                                        "label",
                                        val ->
                                                new LangString(
                                                        ((Literal) val).getLabel(),
                                                        ((Literal) val)
                                                                .getLanguage()
                                                                .orElse(null))))
                        .addBaseUnit(
                                getIfPresent(
                                        bs,
                                        "baseUnit",
                                        compose(
                                                Value::stringValue,
                                                val -> definitions.expectUnitDefinition(val))));
            }
            if (systemOfUnitsDefinition != null) {
                definitions.addSystemOfUnitsDefinition(systemOfUnitsDefinition);
            }
        }
    }

    private void populatePrefixDefinitions(RepositoryConnection con, Definitions definitions) {
        TupleQuery query = con.prepareTupleQuery(queryLoadPrefix);
        try (TupleQueryResult result = query.evaluate()) {
            Iterator<BindingSet> solutions = result.iterator();
            Prefix.Definition prefixDefinition = null;
            String builderIri = null;
            while (solutions.hasNext()) {
                BindingSet bs = solutions.next();
                IRI currentQuantityKindIri = (IRI) bs.getValue("prefix");
                if (prefixDefinition != null
                        && !currentQuantityKindIri.toString().equals(builderIri)) {
                    definitions.addPrefixDefinition(prefixDefinition);
                    prefixDefinition = null;
                }
                if (prefixDefinition == null) {
                    prefixDefinition = makePrefixBuilder(bs);
                    builderIri = bs.getValue("prefix").stringValue();
                }
                if (bs.hasBinding("label")) {
                    Value val = bs.getValue("label");
                    Literal lit = (Literal) val;
                    prefixDefinition.addLabel(
                            new LangString(lit.getLabel(), lit.getLanguage().orElse(null)));
                }
            }
            if (prefixDefinition != null) {
                definitions.addPrefixDefinition(prefixDefinition);
            }
        }
    }

    private void populateFactorUnits(RepositoryConnection con, Definitions definitions) {
        TupleQuery query = con.prepareTupleQuery(queryLoadFactorUnits);
        try (TupleQueryResult result = query.evaluate()) {
            for (BindingSet bs : result) {
                String currentDerivedUnitIri = bs.getValue("derivedUnit").stringValue();
                Unit.Definition derivedUnit =
                        definitions
                                .getUnitDefinition(currentDerivedUnitIri)
                                .orElseThrow(
                                        () ->
                                                new IllegalArgumentException(
                                                        "Found a factor of unknown unit "
                                                                + currentDerivedUnitIri));
                String unitIri = bs.getValue("factorUnit").stringValue();
                Unit.Definition unitDefinition =
                        definitions
                                .getUnitDefinition(unitIri)
                                .orElseThrow(
                                        () ->
                                                new IllegalArgumentException(
                                                        "Found factor unit for unknown unit "
                                                                + unitIri));
                derivedUnit.addFactorUnit(
                        FactorUnit.builder()
                                .unit(unitDefinition)
                                .exponent(((Literal) bs.getValue("exponent")).intValue()));
            }
        }
    }

    private String loadQuery(String queryFile) {
        try (InputStream in =
                Thread.currentThread().getContextClassLoader().getResourceAsStream(queryFile)) {
            if (in == null) {
                throw new IllegalArgumentException(
                        "Classpath resource not found, cannot read query from " + queryFile);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new QudtInitializationException("Error loading qudt query " + queryFile, e);
        }
    }

    private Model loadQudtModel() {
        Model model = new LinkedHashModel();
        RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
        parser.setRDFHandler(new StatementCollector(model));
        loadTtlFile("qudtlib/qudt-prefixes.ttl", parser);
        loadTtlFile("qudtlib/qudt-quantitykinds.ttl", parser);
        loadTtlFile("qudtlib/qudt-units.ttl", parser);
        loadTtlFile("qudtlib/qudt-systems-of-units.ttl", parser);
        return model;
    }

    private static void loadTtlFile(String ttlFile, RDFParser parser) {
        try (InputStream in =
                Thread.currentThread().getContextClassLoader().getResourceAsStream(ttlFile)) {
            parser.parse(in);
        } catch (IOException e) {
            throw new QudtInitializationException("Error loading qudt data from " + ttlFile, e);
        }
    }

    private static <T> T getIfPresent(
            BindingSet bindingSet, String key, Function<Value, T> extractor) {
        if (bindingSet.hasBinding(key)) {
            return extractor.apply(bindingSet.getValue(key));
        }
        return null;
    }

    private static <T1, R1, R2> Function<T1, R2> compose(
            Function<T1, R1> first, Function<R1, R2> second) {
        return (T1 input) -> first.andThen(second).apply(input);
    }

    private static Unit.Definition makeUnitBuilder(BindingSet bs, Definitions definitions) {
        return Unit.definition(bs.getValue("unit").stringValue())
                .dimensionVectorIri(getIfPresent(bs, "dimensionVector", Value::stringValue))
                .conversionMultiplier(
                        getIfPresent(
                                bs,
                                "conversionMultiplier",
                                compose(Value::stringValue, s -> new BigDecimal(s))))
                .conversionOffset(
                        getIfPresent(
                                bs,
                                "conversionOffset",
                                compose(Value::stringValue, s -> new BigDecimal(s))))
                .symbol(getIfPresent(bs, "symbol", Value::stringValue))
                .currencyCode(getIfPresent(bs, "currencyCode", Value::stringValue))
                .currencyNumber(
                        getIfPresent(bs, "currencyNumber", v -> ((SimpleLiteral) v).intValue()))
                .prefix(
                        getIfPresent(
                                bs,
                                "prefix",
                                compose(Value::stringValue, definitions::expectPrefixDefinition)))
                .scalingOf(
                        getIfPresent(
                                bs,
                                "scalingOf",
                                compose(Value::stringValue, definitions::expectUnitDefinition)));
    }

    private static QuantityKind.Definition makeQuantityKindBuilder(BindingSet bs) {
        return QuantityKind.definition(bs.getValue("quantityKind").stringValue())
                .dimensionVectorIri(getIfPresent(bs, "dimensionVector", Value::stringValue))
                .qkdvNumeratorIri(getIfPresent(bs, "qkdvNumerator", Value::stringValue))
                .qkdvDenominatorIri(getIfPresent(bs, "qkdvDenominator", Value::stringValue))
                .symbol(getIfPresent(bs, "symbol", Value::stringValue));
    }

    private static Prefix.Definition makePrefixBuilder(BindingSet bs) {
        return Prefix.definition(bs.getValue("prefix").stringValue())
                .multiplier(
                        getIfPresent(
                                bs,
                                "prefixMultiplier",
                                compose(Value::stringValue, s -> new BigDecimal(s))))
                .symbol(getIfPresent(bs, "symbol", Value::stringValue))
                .ucumCode(getIfPresent(bs, "ucumCode", Value::stringValue));
    }

    private static SystemOfUnits.Definition makeSystemOfUnitsBuilder(BindingSet bs) {
        return SystemOfUnits.definition(bs.getValue("systemOfUnits").stringValue())
                .abbreviation(getIfPresent(bs, "abbreviation", Value::stringValue));
    }
}
