package io.github.qudtlib.model;

import io.github.qudtlib.exception.NotFoundException;
import io.github.qudtlib.exception.QudtInitializationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
    }

    @Override
    public Map<String, Unit> loadUnits() {
        try (RepositoryConnection con = qudtRepository.getConnection()) {
            TupleQuery query = con.prepareTupleQuery(queryLoadUnit);
            return queryUnits(query);
        }
    }

    private Map<String, Unit> queryUnits(TupleQuery query) {
        try (TupleQueryResult result = query.evaluate()) {
            Iterator<BindingSet> solutions = result.iterator();
            Unit unit = null;
            Map<String, Unit> results = new HashMap<>();
            while (solutions.hasNext()) {
                BindingSet bs = solutions.next();
                String currentUnitIri = bs.getBinding("unit").getValue().stringValue();
                if (unit != null && !currentUnitIri.equals(unit.getIri())) {
                    results.put(unit.getIri(), unit);
                    unit = null;
                }
                if (unit == null) {
                    unit = makeUnit(bs);
                }
                if (bs.hasBinding("quantityKind")) {
                    unit.addQuantityKind(bs.getBinding("quantityKind").getValue().stringValue());
                }
                if (bs.hasBinding("label")) {
                    Value val = bs.getValue("label");
                    Literal lit = (Literal) val;
                    unit.addLabel(new LangString(lit.getLabel(), lit.getLanguage().orElse(null)));
                }
            }
            if (unit != null) {
                results.put(unit.getIri(), unit);
            }
            if (results.isEmpty()) {
                throw new NotFoundException(
                        "No units found for unit query with bindings "
                                + bindingsToString(query.getBindings()));
            }
            return results;
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

    @Override
    public Map<String, QuantityKind> loadQuantityKinds() {
        try (RepositoryConnection con = qudtRepository.getConnection()) {
            TupleQuery query = con.prepareTupleQuery(queryLoadQuantityKind);
            try (TupleQueryResult result = query.evaluate()) {
                Iterator<BindingSet> solutions = result.iterator();
                QuantityKind quantityKind = null;
                Map<String, QuantityKind> results = new HashMap<>();
                while (solutions.hasNext()) {
                    BindingSet bs = solutions.next();
                    String currentQuantityKindIri =
                            bs.getBinding("quantityKind").getValue().stringValue();
                    if (quantityKind != null
                            && !currentQuantityKindIri.equals(quantityKind.getIri())) {
                        results.put(quantityKind.getIri(), quantityKind);
                        quantityKind = null;
                    }
                    if (quantityKind == null) {
                        quantityKind = makeQuantityKind(bs);
                    }
                    if (bs.hasBinding("label")) {
                        Value val = bs.getValue("label");
                        Literal lit = (Literal) val;
                        quantityKind.addLabel(
                                new LangString(lit.getLabel(), lit.getLanguage().orElse(null)));
                    }
                    if (bs.hasBinding("broaderQuantityKind")) {
                        String val = bs.getValue("broaderQuantityKind").stringValue();
                        quantityKind.addBroaderQuantityKindIri(val);
                    }
                    if (bs.hasBinding("applicableUnit")) {
                        quantityKind.addApplicableUnitIri(
                                bs.getBinding("applicableUnit").getValue().stringValue());
                    }
                }
                if (quantityKind != null) {
                    results.put(quantityKind.getIri(), quantityKind);
                }
                return results;
            }
        }
    }

    @Override
    public Map<String, Prefix> loadPrefixes() {
        try (RepositoryConnection con = qudtRepository.getConnection()) {
            TupleQuery query = con.prepareTupleQuery(queryLoadPrefix);
            try (TupleQueryResult result = query.evaluate()) {
                Iterator<BindingSet> solutions = result.iterator();
                Prefix prefix = null;
                Map<String, Prefix> results = new HashMap<>();
                while (solutions.hasNext()) {
                    BindingSet bs = solutions.next();
                    IRI currentQuantityKindIri = (IRI) bs.getBinding("prefix").getValue();
                    if (prefix != null
                            && !currentQuantityKindIri.toString().equals(prefix.getIri())) {
                        results.put(prefix.getIri(), prefix);
                        prefix = null;
                    }
                    if (prefix == null) {
                        prefix = makePrefix(bs);
                    }
                    if (bs.hasBinding("label")) {
                        Value val = bs.getValue("label");
                        Literal lit = (Literal) val;
                        prefix.addLabel(
                                new LangString(lit.getLabel(), lit.getLanguage().orElse(null)));
                    }
                }
                if (prefix != null) {
                    results.put(prefix.getIri(), prefix);
                }
                return results;
            }
        }
    }

    @Override
    public void loadFactorUnits(Map<String, Unit> units) {
        try (RepositoryConnection con = qudtRepository.getConnection()) {
            TupleQuery query = con.prepareTupleQuery(queryLoadFactorUnits);
            try (TupleQueryResult result = query.evaluate()) {
                for (BindingSet bs : result) {
                    String currentDerivedUnitIri =
                            bs.getBinding("derivedUnit").getValue().stringValue();
                    Unit derivedUnit = units.get(currentDerivedUnitIri);
                    if (derivedUnit == null) {
                        throw new IllegalArgumentException(
                                "found a factor of unknown unit " + currentDerivedUnitIri);
                    }
                    Optional<FactorUnit> factorUnit = makeFactorUnit(units, bs);
                    factorUnit.ifPresent(derivedUnit::addFactorUnit);
                }
            }
        }
    }

    private String loadQuery(String queryFile) {
        try (InputStream in =
                Thread.currentThread().getContextClassLoader().getResourceAsStream(queryFile)) {
            if (in == null) {
                throw new IllegalArgumentException("Cannot read query from file " + queryFile);
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

    private static Unit makeUnit(BindingSet bs) {
        return new Unit(
                bs.getBinding("unit").getValue().stringValue(),
                bs.hasBinding("prefix") ? bs.getBinding("prefix").getValue().stringValue() : null,
                bs.hasBinding("scalingOf") ? bs.getValue("scalingOf").stringValue() : null,
                bs.hasBinding("dimensionVector")
                        ? bs.getValue("dimensionVector").stringValue()
                        : null,
                bs.hasBinding("conversionMultiplier")
                        ? new BigDecimal(
                                bs.getBinding("conversionMultiplier").getValue().stringValue())
                        : null,
                bs.hasBinding("conversionOffset")
                        ? new BigDecimal(bs.getBinding("conversionOffset").getValue().stringValue())
                        : null,
                bs.hasBinding("symbol") ? bs.getBinding("symbol").getValue().stringValue() : null,
                bs.hasBinding("currencyCode")
                        ? bs.getBinding("currencyCode").getValue().stringValue()
                        : null,
                bs.hasBinding("currencyNumber")
                        ? ((SimpleLiteral) bs.getBinding("currencyNumber").getValue()).intValue()
                        : null);
    }

    private static QuantityKind makeQuantityKind(BindingSet bs) {
        return new QuantityKind(
                bs.getBinding("quantityKind").getValue().stringValue(),
                bs.hasBinding("dimensionVector")
                        ? bs.getBinding("dimensionVector").getValue().stringValue()
                        : null,
                bs.hasBinding("symbol") ? bs.getBinding("symbol").getValue().stringValue() : null);
    }

    private static Prefix makePrefix(BindingSet bs) {
        return new Prefix(
                bs.getBinding("prefix").getValue().stringValue(),
                new BigDecimal(bs.getBinding("prefixMultiplier").getValue().stringValue()),
                bs.getBinding("symbol").getValue().stringValue(),
                bs.hasBinding("ucumCode")
                        ? bs.getBinding("ucumCode").getValue().stringValue()
                        : null);
    }

    private Optional<FactorUnit> makeFactorUnit(Map<String, Unit> units, BindingSet bs) {
        String unitIri = bs.getValue("factorUnit").stringValue();
        Unit unit = units.get(unitIri);
        if (unit == null) {
            logger.info("Found factor unit for inexistent unit {}", unitIri);
            return Optional.empty();
        }
        return Optional.of(new FactorUnit(unit, ((Literal) bs.getValue("exponent")).intValue()));
    }
}
