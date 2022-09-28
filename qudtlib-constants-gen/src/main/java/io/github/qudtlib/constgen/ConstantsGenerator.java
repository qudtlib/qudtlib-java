package io.github.qudtlib.constgen;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import io.github.qudtlib.common.CodeGen;
import io.github.qudlib.common.RdfOps;
import io.github.qudtlib.common.safenames.SafeStringMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 * Generates the Units, QuantityKinds, and Prefixes classes.
 *
 * @author Florian Kleedorfer
 * @since 1.0
 */
public class ConstantsGenerator {
    private final Path outputDir;
    // input data
    private static final String DATA_UNITS = "qudtlib/qudt-units.ttl";
    private static final String DATA_QUANTITYKINDS = "qudtlib/qudt-quantitykinds.ttl";
    private static final String DATA_PREFIXES = "qudtlib/qudt-prefixes.ttl";
    // queries
    private static final String QUERY_UNITS = "query/units.rq";
    private static final String QUERY_QUANTITYKINDS = "query/quantitykinds.rq";
    private static final String QUERY_PREFIXES = "query/prefixes.rq";
    // output
    private static final String DESTINATION_PACKAGE = "io.github.qudtlib.model";
    // template
    private static final String TEMPLATE_FILE = "template/constants.ftl";

    private final SafeStringMapper javaConstantNameMapper = CodeGen.javaConstantMapper();

    public ConstantsGenerator(Path outputDir) {
        this.outputDir = outputDir;
    }

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                throw new IllegalArgumentException("missing argument");
            }
            if (args.length > 1) {
                throw new IllegalArgumentException(" too many arguments");
            }
            String outputDir = args[0];
            ConstantsGenerator generator = new ConstantsGenerator(Path.of(outputDir));
            generator.generate();
        } catch (Exception e) {
            System.err.println("\n\n\tusage: ConstantsGenerator [output-dir]\n\n");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void generate() throws IOException, TemplateException {
        Configuration cfg = CodeGen.getFreemarkerConfiguration();
        generateUnitConstants(cfg);
        generateQuantityKindConstants(cfg);
        generatePrefixConstants(cfg);
    }

    private void generatePrefixConstants(Configuration config)
            throws IOException, TemplateException {
        Map<String, Object> templateVars = getConstantNamesByQuery(QUERY_PREFIXES, DATA_PREFIXES);
        generateJavaFile(config, templateVars, "Prefix", "Prefixes");
    }

    private void generateQuantityKindConstants(Configuration config)
            throws TemplateException, IOException {
        Map<String, Object> templateVars =
                getConstantNamesByQuery(QUERY_QUANTITYKINDS, DATA_QUANTITYKINDS);
        generateJavaFile(config, templateVars, "QuantityKind", "QuantityKinds");
    }

    private void generateUnitConstants(Configuration config) throws TemplateException, IOException {
        Map<String, Object> templateVars = getConstantNamesByQuery(QUERY_UNITS, DATA_UNITS);
        generateJavaFile(config, templateVars, "Unit", "Units");
    }

    private Map<String, Object> getConstantNamesByQuery(String queryFile, String dataFile) {
        String query = RdfOps.loadQuery(queryFile);
        Repository repo = new SailRepository(new MemoryStore());
        Map<String, Object> templateVars = new HashMap<>();
        try (RepositoryConnection con = repo.getConnection()) {
            RdfOps.addStatementsFromFile(con, dataFile);
            try (TupleQueryResult result = con.prepareTupleQuery(query).evaluate()) {
                List<Constant> constants = new ArrayList<>();
                while (result.hasNext()) {
                    BindingSet bindings = result.next();
                    String constName =
                            javaConstantNameMapper.applyMapping(
                                    bindings.getValue("constName").stringValue());
                    String localName = bindings.getValue("localName").stringValue();
                    String label =
                            bindings.getValue("label") == null
                                    ? localName
                                    : bindings.getValue("label").stringValue();
                    Constant constant = new Constant(constName, localName, label);
                    constants.add(constant);
                }
                templateVars.put("constants", constants);
            }
        }
        return templateVars;
    }

    private void generateJavaFile(
            Configuration config, Map<String, Object> templateVars, String type, String typePlural)
            throws IOException, TemplateException {
        RdfOps.message("Generating " + typePlural + ".java");
        File packageFile =
                new File(outputDir + "/" + DESTINATION_PACKAGE.replaceAll(Pattern.quote("."), "/"));
        if (!packageFile.exists()) {
            if (!packageFile.mkdirs()) {
                throw new IOException(
                        "Could not create output dir " + packageFile.getAbsolutePath());
            }
        }
        RdfOps.message("output dir: " + packageFile.getAbsolutePath());
        templateVars.put("type", type);
        templateVars.put("typePlural", typePlural);
        templateVars.put("package", DESTINATION_PACKAGE);
        templateVars.put(
                "valueFactory",
                type.substring(0, 1).toLowerCase() + type.substring(1) + "FromLocalname");
        File outFile = new File(packageFile, typePlural + ".java");
        CodeGen.generateFileFromTemplate(config, TEMPLATE_FILE, templateVars, outFile);
    }
}
