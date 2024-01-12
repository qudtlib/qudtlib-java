package io.github.qudtlib.tools.contribute;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.QudtEntityAtRuntimeAdder;
import io.github.qudtlib.model.*;
import io.github.qudtlib.tools.contribute.model.*;
import io.github.qudtlib.tools.contribute.support.tree.QuantityKindTree;
import io.github.qudtlib.tools.contribute.support.tree.UnitTree;
import io.github.qudtlib.vocab.QUDT;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.common.exception.ValidationException;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.shacl.ShaclSail;
import org.eclipse.rdf4j.sail.shacl.ShaclSailValidationException;

class ToolImpl implements Tool {

    private boolean performShaclValidation;

    public ToolImpl() {
        this(true);
    }

    ToolImpl(boolean performShaclValidation) {
        this.performShaclValidation = performShaclValidation;
        Repository repo;
        if (this.performShaclValidation) {
            repo = new SailRepository(new ShaclSail(new MemoryStore()));
            con = repo.getConnection();
            loadShapes(con);
        } else {
            repo = new SailRepository(new MemoryStore());
            con = repo.getConnection();
        }
    }

    private void loadShapes(RepositoryConnection con) {
        String shaclFile = "/contribute/contribution-shapes.ttl";
        try {
            Reader shaclRules =
                    new InputStreamReader(this.getClass().getResourceAsStream(shaclFile));
            con.begin();
            con.add(shaclRules, "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH);
        } catch (Exception e) {
            System.err.println(
                    String.format(
                            "Error loading SHACL shapes expected on the classpath at %s",
                            shaclFile));
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        con.commit();
    }

    private RepositoryConnection con;
    private List<QuantityKindForContribution> newQuantityKinds = new ArrayList<>();
    private List<UnitForContribution> newUnits = new ArrayList<>();

    void writeRdf(OutputStream out, Predicate<Statement> statementPredicate) {
        try {
            newQuantityKinds.stream().forEach(qk -> this.save(qk, con));
            newUnits.stream().forEach(u -> this.save(u, con));
            con.commit();
            this.writeOut(con, out, statementPredicate);
        } catch (RepositoryException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ShaclSailValidationException) {
                Model validationReportModel =
                        ((ValidationException) cause).validationReportAsModel();
                // use validationReportModel to understand validation violations

                Rio.write(validationReportModel, System.out, RDFFormat.TURTLE);
            }
            throw e;
        }
        con.close();
    }

    public void writeOut(
            RepositoryConnection con, OutputStream out, Predicate<Statement> statementPredicate) {
        RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, out);
        writer.startRDF();
        writer.handleNamespace(
                QudtNamespaces.qudt.getAbbreviationPrefix(), QudtNamespaces.qudt.getBaseIri());
        writer.handleNamespace(
                QudtNamespaces.unit.getAbbreviationPrefix(), QudtNamespaces.unit.getBaseIri());
        writer.handleNamespace(
                QudtNamespaces.systemOfUnits.getAbbreviationPrefix(),
                QudtNamespaces.systemOfUnits.getBaseIri());
        writer.handleNamespace("rdfs", RDFS.NAMESPACE);
        writer.handleNamespace(
                QudtNamespaces.quantityKind.getAbbreviationPrefix(),
                QudtNamespaces.quantityKind.getBaseIri());
        try {
            for (Statement st : con.getStatements(null, null, null)) {
                if (statementPredicate.test(st)) {
                    writer.handleStatement(st);
                }
            }
            writer.endRDF();
        } catch (RDFHandlerException e) {
            // oh no, do something!
        }
    }

    @Override
    public void writeOut(Model model, OutputStream out) {
        this.writeOut(model, out, s -> true);
    }

    @Override
    public void writeOut(Model model, OutputStream out, Predicate<Statement> statementPredicate) {
        RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, out);
        writer.startRDF();
        writer.handleNamespace(
                QudtNamespaces.qudt.getAbbreviationPrefix(), QudtNamespaces.qudt.getBaseIri());
        writer.handleNamespace(
                QudtNamespaces.unit.getAbbreviationPrefix(), QudtNamespaces.unit.getBaseIri());
        writer.handleNamespace(
                QudtNamespaces.systemOfUnits.getAbbreviationPrefix(),
                QudtNamespaces.systemOfUnits.getBaseIri());
        writer.handleNamespace("rdfs", RDFS.NAMESPACE);
        writer.handleNamespace(
                QudtNamespaces.quantityKind.getAbbreviationPrefix(),
                QudtNamespaces.quantityKind.getBaseIri());
        try {
            for (Statement st : model.getStatements(null, null, null)) {
                if (statementPredicate.test(st)) {
                    writer.handleStatement(st);
                }
            }
            writer.endRDF();
        } catch (RDFHandlerException e) {
            // oh no, do something!
        }
    }

    @Override
    public Unit addDerivedUnit(FactorUnits factorUnits, Consumer<Unit.Definition> unitConfigurer) {
        return addDerivedUnit(factorUnits, unitConfigurer, null, null);
    }

    public Unit addDerivedUnit(
            FactorUnits factorUnits,
            Consumer<Unit.Definition> unitConfigurer,
            Consumer<UnitMetadata.Builder> metadataConfigurer) {
        return addDerivedUnit(factorUnits, unitConfigurer, metadataConfigurer, null);
    }

    @Override
    public Unit addDerivedUnit(
            FactorUnits factorUnits,
            Consumer<Unit.Definition> unitConfigurer,
            Consumer<UnitMetadata.Builder> metadataConfigurer,
            String nonstandardLocalname) {
        UnitForContribution.Builder builder =
                UnitForContribution.builder(factorUnits, nonstandardLocalname);
        unitConfigurer.accept(builder.unit());
        if (metadataConfigurer != null) {
            metadataConfigurer.accept(builder.metadata());
        }
        UnitForContribution unit = (UnitForContribution) builder.build();
        newUnits.add(unit);
        QudtEntityAtRuntimeAdder.addUnit(unit.getUnit());
        return unit.getUnit();
    }

    @Override
    public QuantityKind addQuantityKind(
            FactorUnits factorUnits,
            String localname,
            Consumer<QuantityKind.Definition> quantityKindConfigurer) {
        return addQuantityKind(factorUnits, localname, quantityKindConfigurer, null);
    }

    @Override
    public QuantityKind addQuantityKind(
            FactorUnits factorUnits,
            String localname,
            Consumer<QuantityKind.Definition> quantityKindConfigurer,
            Consumer<QuantityKindMetadata.Builder> metadataConfigurer) {
        QuantityKindForContribution.Builder builder =
                QuantityKindForContribution.builder(factorUnits, localname);
        quantityKindConfigurer.accept(builder.quantityKind());
        if (metadataConfigurer != null) {
            metadataConfigurer.accept(builder.metadata());
        }
        QuantityKindForContribution quantityKind = builder.build();
        newQuantityKinds.add(quantityKind);
        QudtEntityAtRuntimeAdder.addQuantityKind(quantityKind.getQuantityKind());
        return quantityKind.getQuantityKind();
    }

    @Override
    public boolean checkUnitExists(FactorUnits factorUnits) {
        return checkUnitExists(factorUnits, DerivedUnitSearchMode.BEST_MATCH);
    }

    @Override
    public boolean checkUnitExists(FactorUnits factorUnits, DerivedUnitSearchMode mode) {
        List<Unit> units = Qudt.unitsFromFactorUnits(mode, factorUnits.getFactorUnits());
        System.err.println(
                "Checking if a unit exists in Qudt for factor units " + factorUnits.toString());
        if (units.isEmpty()) {
            System.err.println("  --> none found");
            return false;
        } else {
            System.err.println("  --> found these:");
            for (Unit unit : units) {
                printQuantityKinds(
                        "    " + QudtNamespaces.unit.abbreviate(unit.getIri()),
                        "   quantity kinds:\n"
                                + QuantityKindTree.formatQuantityKindForest(
                                        unit.getQuantityKinds()));
            }
            return true;
        }
    }

    public Set<Unit> findUnitBySymbolOrUcumCode(String symbol) {
        Set<Unit> units =
                Qudt.allUnits().stream()
                        .filter(
                                u ->
                                        u.getSymbol().map(s -> s.equals(symbol)).orElse(false)
                                                || u.getUcumCode()
                                                        .map(uc -> uc.equals(symbol))
                                                        .orElse(false))
                        .collect(Collectors.toSet());
        System.err.println(
                String.format(
                        "Checking if a unit exists in Qudt with symbol or ucumCode '%s'", symbol));
        if (units.isEmpty()) {
            System.err.println("  --> none found");
        } else {
            System.err.println("  --> found these:");
            for (Unit unit : units) {
                printQuantityKinds(
                        "    " + QudtNamespaces.unit.abbreviate(unit.getIri()),
                        "   quantity kinds:\n"
                                + QuantityKindTree.formatQuantityKindForest(
                                        unit.getQuantityKinds()));
            }
        }
        return units;
    }

    @Override
    public List<Unit> listUnitsWithSameDimensions(Unit unit) {
        System.err.println("Units with same dimension vector as " + unit.toString());
        List<Unit> ret = Qudt.unitsWithSameFractionalDimensionVector(unit);
        ret.sort(Comparator.comparing(Unit::getIri));
        if (ret.isEmpty()) {
            System.err.println("  --> none found");
        } else {
            System.err.println("  --> found these:");
            for (Unit u : ret) {
                printQuantityKinds(
                        "    " + QudtNamespaces.unit.abbreviate(u.getIri()),
                        "   quantity kinds:\n"
                                + QuantityKindTree.formatQuantityKindForest(u.getQuantityKinds()));
            }
        }
        return ret;
    }

    @Override
    public boolean checkQuantityKindExists(FactorUnits factorUnits) {
        System.err.println(
                "Checking if a quantity kind exists in Qudt for factor units "
                        + factorUnits.toString()
                        + ", dim vector "
                        + factorUnits.getDimensionVectorIri());
        List<QuantityKind> quantityKinds =
                searchQuantityKinds(
                        qk -> {
                            boolean result =
                                    qk.getDimensionVectorIri()
                                            .map(v -> v.equals(factorUnits.getDimensionVectorIri()))
                                            .orElse(false);
                            if (!result) {
                                return false;
                            }
                            if (qk.getQkdvDenominatorIri().isPresent()
                                    && qk.getQkdvNumeratorIri().isPresent()) {
                                return factorUnits.hasQkdvDenominatorIri(
                                                qk.getQkdvDenominatorIri().get())
                                        && factorUnits.hasQkdvNumeratorIri(
                                                qk.getQkdvNumeratorIri().get());
                            }
                            return true;
                        });
        printQuantityKindSearchResult(quantityKinds);
        return quantityKinds != null;
    }

    @Override
    public List<QuantityKind> searchQuantityKinds(String localnameOrLabelRegex) {
        System.err.println(
                String.format(
                        "Checking if a quantity kind exists for search regex '%s'",
                        localnameOrLabelRegex));
        try {
            Pattern p =
                    Pattern.compile(
                            localnameOrLabelRegex, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
            List<QuantityKind> quantityKinds =
                    searchQuantityKinds(
                            qk ->
                                    qk.getLabels().stream()
                                                    .anyMatch(
                                                            ls -> p.matcher(ls.getString()).find())
                                            || p.matcher(
                                                            QudtNamespaces.quantityKind
                                                                    .getLocalName(qk.getIri()))
                                                    .find());
            printQuantityKindSearchResult(quantityKinds);
            return quantityKinds;
        } catch (Exception e) {
            System.err.println(
                    "Error searching for pattern '"
                            + localnameOrLabelRegex
                            + "': "
                            + e.getMessage());
        }
        return List.of();
    }

    @Override
    public String generateJavaCodeStringForFactorUnits(FactorUnits factorUnits) {
        StringBuilder sb = new StringBuilder();
        sb.append("FactorUnits.ofFactorUnitSpec(");
        for (FactorUnit fu : factorUnits.getFactorUnits()) {
            String unitConstant = fu.getUnit().getIriLocalname().replaceAll("-", "__");
            if (fu.getUnit().isCurrencyUnit()) {
                unitConstant = unitConstant + "_Currency";
            }
            sb.append("Qudt.Units.")
                    .append(unitConstant)
                    .append(", ")
                    .append(fu.getExponent())
                    .append(", ");
        }
        sb.deleteCharAt(sb.length() - 2);
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void addUnitsForUcumCodeBestEffort(String ucumCode, boolean force) {
        List<FactorUnits> factorUnitsList = parseUcumCodeToFactorUnits(ucumCode);
        for (FactorUnits factorUnits : factorUnitsList) {
            addUnitsForFactorUnitsBestEffort(factorUnits, force);
        }
    }

    @Override
    public void addUnitsForFactorUnitsBestEffort(FactorUnits factorUnits, boolean force) {
        Set<Unit> matchingunits = findExistingQudtUnitsForFactorUnits(factorUnits);
        if (matchingunits.isEmpty() || force) {
            System.err.println(String.format("Adding unit for factors %s", factorUnits));
            FactorUnits unscaled = new FactorUnits(Qudt.unscale(factorUnits.getFactorUnits()));
            if ((!unscaled.equals(factorUnits))
                    && Qudt.unitsFromFactorUnits(
                                    DerivedUnitSearchMode.BEST_MATCH, unscaled.getFactorUnits())
                            .isEmpty()) {
                addDerivedUnitBestEffort(unscaled);
            }
            addDerivedUnitBestEffort(factorUnits);
        } else {
            System.err.println(
                    String.format(
                            "Not adding unit for factors %s - unit already exists: %s",
                            factorUnits, matchingunits.stream().findFirst().get()));
        }
    }

    @Override
    public Set<Unit> findExistingQudtUnitsForFactorUnits(FactorUnits factorUnits) {
        Set<Unit> matchingunits =
                Qudt.allUnits().stream()
                        .filter(
                                u ->
                                        (factorUnits
                                                .streamLocalnamePossibilities()
                                                .anyMatch(u.getIriLocalname()::equals)))
                        .collect(Collectors.toSet());
        return matchingunits;
    }

    @Override
    public List<FactorUnits> parseUcumCodeToFactorUnits(String ucumCode) {
        Pattern p = Pattern.compile("^([^-]+)(-?\\d)?$");
        String[] factorUnitUcumCodes = ucumCode.split("\\.");
        List<List<FactorUnit>> factorUnitLists = new ArrayList<>();
        factorUnitLists.add(new ArrayList<>());
        for (String factorUnitUcumCode : factorUnitUcumCodes) {
            Matcher m = p.matcher(factorUnitUcumCode);
            if (m.matches()) {
                String unitStr = m.group(1);
                int exponent = 1;
                String exponentStr = m.group(2);
                if (exponentStr != null) {
                    exponent = Integer.parseInt(exponentStr);
                }
                List<Unit> units =
                        Qudt.allUnits().stream()
                                .filter(
                                        u ->
                                                u.getUcumCode()
                                                                .map(s -> s.equals(unitStr))
                                                                .orElse(false)
                                                        || u.getSymbol()
                                                                .map(s -> s.equals(unitStr))
                                                                .orElse(false))
                                .collect(Collectors.toList());

                if (units.isEmpty()) {
                    System.err.println(
                            String.format(
                                    "Cannot find QUDT unit for factor unit %s (exp: %d) of %s",
                                    unitStr, exponent, ucumCode));
                    break;
                }
                List<List<FactorUnit>> newList = new ArrayList<>();
                for (List<FactorUnit> fus : factorUnitLists) {
                    for (Unit matchedUnit : units) {
                        List<FactorUnit> variant = new ArrayList<>(fus);
                        variant.add(new FactorUnit(matchedUnit, exponent));
                        newList.add(variant);
                    }
                }
                factorUnitLists = newList;
            } else {
                System.err.println(
                        String.format(
                                "cannot identify factor unit of %s from this part: %s: ",
                                ucumCode, factorUnitUcumCode));
                break;
            }
        }
        return factorUnitLists.stream()
                .map(ful -> new FactorUnits(ful))
                .collect(Collectors.toList());
    }

    @Override
    public void printFactorUnitTree(Unit unit) {
        printFactorUnitTree(unit, null);
    }

    @Override
    public void printFactorUnitTree(Unit unit, Function<FactorUnit, String> unitFormatter) {
        printFactorUnitTree(unit, null, System.err);
    }

    @Override
    public void printFactorUnitTree(
            Unit unit, Function<FactorUnit, String> unitFormatter, OutputStream out) {
        PrintStream writer = new PrintStream(out);
        writer.println("factor unit tree for unit " + unit.toString());
        writer.println(UnitTree.formatFactorUnitTree(unit, unitFormatter));
    }

    private void addDerivedUnitBestEffort(FactorUnits factorUnits) {
        System.err.println(String.format("\t\t Adding unit for %s", factorUnits));
        String dimensionVector = factorUnits.getDimensionVectorIri();
        List<QuantityKind> matchingQks =
                Qudt.allQuantityKinds().stream()
                        .filter(
                                qk ->
                                        qk.getDimensionVectorIri()
                                                .map(dv -> dv.equals(dimensionVector))
                                                .orElse(false))
                        .collect(Collectors.toList());
        if (matchingQks.isEmpty()) {
            QuantityKind newQk =
                    this.addQuantityKind(
                            factorUnits,
                            "New_Quantity_Kind_for_" + factorUnits.getLocalname(),
                            qk -> qk.addLabel("TODO:label in English", "en"),
                            meta ->
                                    meta.plainTextDescription(
                                                    "TODO:plain text description in English")
                                            .qudtInformativeReference(
                                                    "TODO: URI with more information, preferably in English"));
            matchingQks.add(newQk);
        }
        this.addDerivedUnit(
                factorUnits,
                unitDef -> {
                    unitDef.addSystemOfUnits(SystemsOfUnits.SI);
                    if (matchingQks.isEmpty()) {
                        unitDef.addQuantityKind(QuantityKinds.Dimensionless);
                    } else {
                        matchingQks.forEach(qk -> unitDef.addQuantityKind(qk));
                    }
                },
                meta -> meta.plainTextDescription("TODO: plain text description in English"));
    }

    public List<QuantityKind> searchQuantityKinds(Predicate<QuantityKind> filter) {
        List<QuantityKind> quantityKinds =
                Qudt.allQuantityKinds().stream().filter(filter).collect(Collectors.toList());
        return quantityKinds;
    }

    private void printQuantityKindSearchResult(List<QuantityKind> quantityKinds) {
        if (quantityKinds.isEmpty()) {
            System.err.println("  --> none found");
        } else {
            printQuantityKinds(
                    "  --> found these:", QuantityKindTree.formatQuantityKindForest(quantityKinds));
        }
    }

    private void printQuantityKinds(String x, String quantityKinds) {
        System.err.println(x);
        System.err.println(quantityKinds);
    }

    private void save(
            QuantityKindForContribution quantityKindForContribution, RepositoryConnection con) {
        QuantityKind quantityKind = quantityKindForContribution.getQuantityKind();
        suggestConnectedQuantityKindsIfIsolated(quantityKind);
        ModelBuilder mb = new ModelBuilder();
        mb.subject(quantityKind.getIri())
                .add(RDF.TYPE, QUDT.QuantityKind)
                .add(RDFS.ISDEFINEDBY, iri("http://qudt.org/2.1/vocab/quantitykind"));
        for (QuantityKind qk : quantityKind.getBroaderQuantityKinds()) {
            mb.add(SKOS.BROADER, iri(qk.getIri()));
        }
        addIfPresent(
                mb,
                QUDT.hasDimensionVector,
                quantityKind.getDimensionVectorIri().map(ToolImpl::iri).orElse(null));
        addIfPresent(
                mb,
                QUDT.symbol,
                quantityKind.getSymbol().map(ToolImpl::stringLiteral).orElse(null));
        for (QuantityKind exactMatch : quantityKind.getExactMatches()) {
            mb.add(QUDT.exactMatch, iri(exactMatch.getIri()));
        }
        for (LangString label : quantityKind.getLabels()) {
            mb.add(
                    RDFS.LABEL,
                    ToolImpl.stringLiteral(label.getString(), label.getLanguageTag().orElse(null)));
        }
        saveQuantityKindMetadata(mb, quantityKindForContribution.getQuantityKindMetadata());
        Model model = mb.build();
        System.err.println("Adding these triples: ");
        this.writeOut(model, System.err);
        con.add(model, new Resource[] {});
    }

    private static void suggestConnectedQuantityKindsIfIsolated(QuantityKind quantityKind) {
        if (quantityKind.getBroaderQuantityKinds().isEmpty()
                && quantityKind.getExactMatches().isEmpty()) {
            System.err.println("\n" + QudtNamespaces.unit.abbreviate(quantityKind.getIri()));
            System.err.println(
                    " --- Note: no relation with other quantitykinds (dimvector: "
                            + quantityKind.getDimensionVectorIri().orElse("[none]")
                            + ")");
            List<QuantityKind> applicableQuantityKinds =
                    Qudt.allQuantityKinds().stream()
                            .filter(
                                    qk ->
                                            qk.getDimensionVectorIri()
                                                    .map(
                                                            dv ->
                                                                    dv.equals(
                                                                            quantityKind
                                                                                    .getDimensionVectorIri()
                                                                                    .get()))
                                                    .orElse(false))
                            .collect(Collectors.toList());
            if (applicableQuantityKinds.isEmpty()) {
                System.err.println(
                        "   ... and no Quantitykinds with same dimension vector found in QUDT");
            } else {
                System.err.println("   The following quantitykinds might be applicable:");
                System.err.println(
                        QuantityKindTree.formatQuantityKindForest(applicableQuantityKinds));
            }
        }
    }

    private void save(UnitForContribution unitForContribution, RepositoryConnection con) {
        Unit unit = unitForContribution.getUnit();
        suggestQuantityKindsIfNotPresent(unit);
        warnIfQuantityKindHasDifferentDimensionVector(unit);
        ModelBuilder mb = new ModelBuilder();
        mb.subject(unit.getIri())
                .add(RDF.TYPE, QUDT.Unit)
                .add(RDFS.ISDEFINEDBY, iri("http://qudt.org/2.1/vocab/unit"));
        for (SystemOfUnits sou : unit.getUnitOfSystems()) {
            mb.add(QUDT.applicableSystem, iri(sou.getIri()));
        }
        for (QuantityKind qk : unit.getQuantityKinds()) {
            mb.add(QUDT.hasQuantityKind, iri(qk.getIri()));
        }
        addIfPresent(
                mb,
                QUDT.conversionMultiplier,
                unit.getConversionMultiplier().map(ToolImpl::numericLiteral).orElse(null));
        addIfPresent(
                mb,
                QUDT.conversionOffset,
                unit.getConversionOffset().map(ToolImpl::numericLiteral).orElse(null));
        addIfPresent(
                mb,
                QUDT.hasDimensionVector,
                unit.getDimensionVectorIri().map(ToolImpl::iri).orElse(null));
        addIfPresent(mb, QUDT.symbol, unit.getSymbol().map(ToolImpl::stringLiteral).orElse(null));
        addIfPresent(
                mb,
                QUDT.ucumCode,
                unit.getUcumCode()
                        .map(code -> typedLiteral(new TypedLiteral(code, QUDT.UCUMcs.toString())))
                        .orElse(null));
        for (Unit exactMatch : unit.getExactMatches()) {
            mb.add(QUDT.exactMatch, iri(exactMatch.getIri()));
        }
        for (LangString label : unit.getLabels()) {
            mb.add(
                    RDFS.LABEL,
                    ToolImpl.stringLiteral(label.getString(), label.getLanguageTag().orElse(null)));
        }
        saveCommonMetadata(mb, unitForContribution.getMetadata());
        Model model = mb.build();
        System.err.println("Adding these triples: ");
        this.writeOut(model, System.err);
        con.add(model, new Resource[] {});
    }

    private static void saveQuantityKindMetadata(ModelBuilder mb, QuantityKindMetadata metadata) {
        saveCommonMetadata(mb, metadata);
    }

    private static void saveUnitMetadata(ModelBuilder mb, UnitMetadata metadata) {
        saveCommonMetadata(mb, metadata);
        addIfPresent(mb, QUDT.ucumCode, typedLiteral(metadata.getQudtUcumCode()));
        addIfPresent(mb, QUDT.iec61360Code, stringLiteral(metadata.getQudtIec61360Code()));
        addIfPresent(mb, QUDT.uneceCommonCode, stringLiteral(metadata.getQudtUneceCommonCode()));
        addIfPresent(mb, QUDT.omUnit, metadata.getQudtOmUnit());
    }

    private static void saveCommonMetadata(ModelBuilder mb, CommonEntityMetadata metadata) {
        addIfPresent(mb, DCTERMS.DESCRIPTION, typedLiteral(metadata.getDcTermsDescription()));
        addIfPresent(mb, QUDT.expression, typedLiteral(metadata.getQudtExpression()));
        metadata.getQudtInformativeReference()
                .forEach(tl -> addIfPresent(mb, QUDT.informativeReference, typedLiteral(tl)));
        addIfPresent(
                mb,
                QUDT.isoNormativeReference,
                typedLiteral(metadata.getQudtIsoNormativeReference()));
        addIfPresent(
                mb, QUDT.plainTextDescription, stringLiteral(metadata.getPlainTextDescription()));
        addIfPresent(mb, RDFS.SEEALSO, metadata.getRdfsSeeAlso());
        addIfPresent(mb, RDFS.ISDEFINEDBY, metadata.getRdfsIsDefinedBy());
        addIfPresent(mb, QUDT.latexSymbol, typedLiteral(metadata.getLatexSymbol()));
        addIfPresent(mb, QUDT.latexDefinition, typedLiteral(metadata.getLatexDefinition()));
        addIfPresent(mb, QUDT.dbpediaMatch, typedLiteral(metadata.getQudtDbpediaMatch()));
    }

    private static Value typedLiteral(TypedLiteral typedLiteral) {
        return Optional.ofNullable(typedLiteral)
                .map(
                        lit ->
                                SimpleValueFactory.getInstance()
                                        .createLiteral(
                                                lit.getLiteral(),
                                                SimpleValueFactory.getInstance()
                                                        .createIRI(lit.getTypeIRI())))
                .orElse(null);
    }

    private static void warnIfQuantityKindHasDifferentDimensionVector(Unit unit) {
        for (QuantityKind quantityKind : unit.getQuantityKinds()) {
            if (!quantityKind
                    .getDimensionVectorIri()
                    .get()
                    .equals(unit.getDimensionVectorIri().get())) {
                System.err.println("\n" + QudtNamespaces.unit.abbreviate(unit.getIri()));
                System.err.println(
                        String.format(
                                " --- Note: dimension vector of quantitykind %s \n\t%s differs from unit dimension vector \n\t%s",
                                QudtNamespaces.quantityKind.abbreviate(quantityKind.getIri()),
                                quantityKind.getDimensionVectorIri().get(),
                                unit.getDimensionVectorIri().get()));
            }
        }
    }

    private static void suggestQuantityKindsIfNotPresent(Unit unit) {
        if (unit.getQuantityKinds().isEmpty()) {
            System.err.println("\n" + QudtNamespaces.unit.abbreviate(unit.getIri()));
            System.err.println(
                    " --- Note: no quantitykinds set for unit (dimvector: "
                            + unit.getDimensionVectorIri().orElse("[none]")
                            + ")");
            List<QuantityKind> applicableQuantityKinds =
                    Qudt.allQuantityKinds().stream()
                            .filter(
                                    qk ->
                                            qk.getDimensionVectorIri()
                                                    .map(
                                                            dv ->
                                                                    dv.equals(
                                                                            unit.getDimensionVectorIri()
                                                                                    .get()))
                                                    .orElse(false))
                            .collect(Collectors.toList());
            if (applicableQuantityKinds.isEmpty()) {
                System.err.println("   ... and no applicable Quantitykinds found in QUDT");
            } else {
                System.err.println(
                        "   The following quantitykinds might be applicable: \n"
                                + applicableQuantityKinds.stream()
                                        .map(
                                                qk ->
                                                        QudtNamespaces.quantityKind.abbreviate(
                                                                qk.getIri()))
                                        .collect(Collectors.joining("\n   ", "   ", "")));
            }
        }
    }

    private static void addIfPresent(ModelBuilder mb, IRI property, Value value) {
        if (value != null) {
            mb.add(property, value);
        }
    }

    private static IRI iri(String iriString) {
        return Optional.ofNullable(iriString)
                .map(is -> SimpleValueFactory.getInstance().createIRI(is))
                .orElse(null);
    }

    private static Literal stringLiteral(String literalValue) {
        return stringLiteral(literalValue, null);
    }

    private static Literal stringLiteral(String literalValue, String languageTag) {
        if (literalValue == null) {
            return null;
        }
        if (languageTag == null) {
            return SimpleValueFactory.getInstance().createLiteral(literalValue);
        } else {
            return SimpleValueFactory.getInstance().createLiteral(literalValue, languageTag);
        }
    }

    private static Literal numericLiteral(BigDecimal number) {
        return Optional.ofNullable(number)
                .map(num -> SimpleValueFactory.getInstance().createLiteral(num))
                .orElse(null);
    }
}
