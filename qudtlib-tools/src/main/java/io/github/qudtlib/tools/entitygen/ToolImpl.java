package io.github.qudtlib.tools.entitygen;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.QudtEntityAtRuntimeAdder;
import io.github.qudtlib.model.*;
import io.github.qudtlib.tools.entitygen.model.*;
import io.github.qudtlib.tools.entitygen.support.QuantityKindTree;
import io.github.qudtlib.vocab.QUDT;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

class ToolImpl implements Tool {
    ToolImpl() {
        Repository repo = new SailRepository(new MemoryStore());
        con = repo.getConnection();
    }

    private RepositoryConnection con;
    private List<QuantityKindForContribution> newQuantityKinds = new ArrayList<>();
    private List<UnitForContribution> newUnits = new ArrayList<>();

    void writeRdf(OutputStream out) {
        newQuantityKinds.stream().forEach(qk -> ToolImpl.save(qk, con));
        newUnits.stream().forEach(u -> ToolImpl.save(u, con));
        con.commit();
        ToolImpl.writeOut(con, out);
        con.close();
    }

    private static void writeOut(RepositoryConnection con, OutputStream out) {
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
                writer.handleStatement(st);
            }
            writer.endRDF();
        } catch (RDFHandlerException e) {
            // oh no, do something!
        }
    }

    @Override
    public Unit addDerivedUnit(FactorUnits factorUnits, Consumer<Unit.Definition> unitConfigurer) {
        return addDerivedUnit(factorUnits, unitConfigurer, null);
    }

    @Override
    public Unit addDerivedUnit(
            FactorUnits factorUnits,
            Consumer<Unit.Definition> unitConfigurer,
            Consumer<UnitMetadata.Builder> metadataConfigurer) {
        UnitForContribution.Builder builder = UnitForContribution.builder(factorUnits);
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
        Set<Unit> units = Qudt.derivedUnitsFromFactorUnits(mode, factorUnits.getFactorUnits());
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

    private static void save(
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
        con.add(mb.build());
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

    private static void save(UnitForContribution unitForContribution, RepositoryConnection con) {
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
        for (Unit exactMatch : unit.getExactMatches()) {
            mb.add(QUDT.exactMatch, iri(exactMatch.getIri()));
        }
        for (LangString label : unit.getLabels()) {
            mb.add(
                    RDFS.LABEL,
                    ToolImpl.stringLiteral(label.getString(), label.getLanguageTag().orElse(null)));
        }
        saveCommonMetadata(mb, unitForContribution.getMetadata());
        con.add(mb.build());
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
