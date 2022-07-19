package com.github.qudtlib.data;

import com.github.qudlib.common.RdfOps;
import com.github.qudtlib.vocab.QUDT;
import com.github.qudtlib.vocab.QUDTX;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.RepositoryUtil;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 * Processes the original QUDT TTL files and adds/removes triples to suit the needs of QUDTLib.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class DataGenerator {
    private final Path outputDir;
    // QUDT files
    private static final String UNITS_FILE = "qudt/vocab/unit/VOCAB_QUDT-UNITS-ALL-v2.1.ttl";
    private static final String PREFIXES_FILE = "qudt/vocab/prefixes/VOCAB_QUDT-PREFIXES-v2.1.ttl";
    private static final String QUANTITYKINDS_FILE =
            "qudt/vocab/quantitykinds/VOCAB_QUDT-QUANTITY-KINDS-ALL-v2.1.ttl";
    // generated output files
    private static final String UNITS_OUTFILE = "qudt-units.ttl";
    private static final String PREFIXES_OUTFILE = "qudt-prefixes.ttl";
    private static final String QUANTITYKINDS_OUTFILE = "qudt-quantitykinds.ttl";
    // queries
    private static final String FACTOR_UNITS_QUERY = "factorUnit.rq";
    private static final String IS_SCALING_OF_QUERY = "isScalingOf.rq";
    private static final String MISSING_UNITS_QUERY = "missing-units.rq";
    private static final String REMOVE_KILOGM_SCALINGS_QUERY = "remove-kiloGM-scalings.rq";
    // additional data
    private static final String SI_BASE_UNITS_DATA = "si-base-units.ttl";
    private static final String UNITS_EXPECTED_DATA = "tmpExpected/qudt-unit.ttl";

    public DataGenerator(String outputDir) {

        this.outputDir = Path.of(outputDir);
        RdfOps.message("Using output dir " + this.outputDir.toFile().getAbsolutePath());
        if (!this.outputDir.toFile().exists()) {
            if (!this.outputDir.toFile().mkdirs()) {
                throw new IllegalStateException("Unable to generate output dir " + outputDir);
            }
        }
    }

    public static void main(String[] args) {
        DataGenerator generator;
        try {
            if (args.length == 0) {
                throw new IllegalArgumentException("missing argument");
            }
            if (args.length > 1) {
                throw new IllegalArgumentException(" too many arguments");
            }
            String outputDir = args[0];
            generator = new DataGenerator(outputDir);
        } catch (Exception e) {
            System.err.println("\n\n\tusage: Datagenerator [output-dir]\n\n");
            e.printStackTrace();
            System.exit(1);
            return;
        }
        generator.processUnits();
        generator.processPrefixes();
        generator.processQuantityKinds();
        RdfOps.message("Done generating qudtlib-flavored TTL files");
    }

    private void processPrefixes() {
        RdfOps.message("Processing QUDT prefixes");
        Repository outputRepo = new SailRepository(new MemoryStore());
        try (RepositoryConnection outputCon = outputRepo.getConnection()) {
            RdfOps.addStatementsFromFile(outputCon, PREFIXES_FILE);
            RdfOps.writeTurtleFile(outputCon, outFile(PREFIXES_OUTFILE));
        }
    }

    private void processQuantityKinds() {
        RdfOps.message("Processing QUDT quantity kinds");
        Repository outputRepo = new SailRepository(new MemoryStore());
        try (RepositoryConnection outputCon = outputRepo.getConnection()) {
            RdfOps.addStatementsFromFile(outputCon, QUANTITYKINDS_FILE);
            RdfOps.writeTurtleFile(outputCon, outFile(QUANTITYKINDS_OUTFILE));
        }
    }

    void processUnits() {
        RdfOps.message("Generating additional triples for QUDT units...");
        Repository inputRepo = new SailRepository(new MemoryStore());
        Repository outputRepo = new SailRepository(new MemoryStore());
        try (RepositoryConnection inputCon = inputRepo.getConnection()) {
            try (RepositoryConnection outputCon = outputRepo.getConnection()) {
                RdfOps.addStatementsFromFile(inputCon, UNITS_FILE);
                RdfOps.updateDataUsingQuery(inputCon, REMOVE_KILOGM_SCALINGS_QUERY);
                RdfOps.copyData(inputCon, outputCon);
                RdfOps.addStatementsFromFile(inputCon, PREFIXES_FILE);
                RdfOps.addStatementsFromFile(outputCon, SI_BASE_UNITS_DATA);
                RdfOps.addStatementsFromFile(inputCon, SI_BASE_UNITS_DATA);
                RdfOps.addDataUsingQuery(outputCon, FACTOR_UNITS_QUERY, outputCon);
                RdfOps.addDataUsingQuery(outputCon, MISSING_UNITS_QUERY, inputCon, outputCon);
                RdfOps.addDataUsingQuery(inputCon, IS_SCALING_OF_QUERY, outputCon);
                RdfOps.writeTurtleFile(outputCon, outFile(UNITS_OUTFILE));
                // comparison with expected is useful during version upgrades, keep it commented out
                // during normal execution
                // compareWithExpected(outputCon);
            }
        }
    }

    private void compareWithExpected(RepositoryConnection outputCon) {
        if (Thread.currentThread().getContextClassLoader().getResourceAsStream(UNITS_EXPECTED_DATA)
                == null) {
            RdfOps.message("no expected data provided - skipping comparison ");
            return;
        }
        Model expected = RdfOps.loadTurtleToModel(UNITS_EXPECTED_DATA);
        Model actual =
                new LinkedHashModel(
                        outputCon.getStatements(null, null, null, (Resource) null).stream()
                                .collect(Collectors.toList()));
        RdfOps.message(
                "\n\nExpected data subset of generated: " + Models.isomorphic(actual, expected));
        compareForProperty(expected, actual, QUDT.isScalingOf);
        compareForProperty(expected, actual, QUDTX.factorUnit);
    }

    private void compareForProperty(Model expected, Model actual, IRI property) {
        RdfOps.message("\nDiff for generated property " + property + ":\n");
        Collection<? extends Statement> onlyInExpected =
                RepositoryUtil.difference(
                        expected.filter(null, property, null), actual.filter(null, property, null));
        Collection<? extends Statement> onlyInActual =
                RepositoryUtil.difference(
                        actual.filter(null, property, null), expected.filter(null, property, null));
        RdfOps.message("Only in expected model:");
        for (Statement s : onlyInExpected) {
            RdfOps.message(s.toString());
        }
        RdfOps.message("\n\nOnly in actual model:");
        for (Statement s : onlyInActual) {
            RdfOps.message(s.toString());
        }
    }

    private Path outFile(String file) {
        return this.outputDir.resolve(file);
    }
}
