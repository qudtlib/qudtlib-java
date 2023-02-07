package io.github.qudtlib;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import io.github.qudtlib.common.CodeGen;
import io.github.qudtlib.common.RdfOps;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Generates a Java class that instantiates all QUDT individuals and relationships needed for
 * QUDTLib from hardcoded data, i.e. without the need to process RDF.
 *
 * <p>This generator uses a functioning QUDTLib implementation that instantiates all individuals
 * from RDF (module <code>qudtlib-main-rdf</code>) to generate the 'hardcoded' class, which can
 * subsequently be used to initialize the internal model (as is done in module <code>qudtlib</code>
 * ).
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class HardcodedModelGenerator {
    private final Path outputDir;
    // output
    private static final String DESTINATION_PACKAGE = "io.github.qudtlib.init";
    private static final String FILENAME = "InitializerImpl.java";
    // template
    private static final String TEMPLATE_FILE = "template/InitializerImpl.ftl";

    public HardcodedModelGenerator(Path outputDir) {
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
            HardcodedModelGenerator generator = new HardcodedModelGenerator(Path.of(outputDir));
            generator.generate();
        } catch (Exception e) {
            System.err.println("\n\n\tusage: HardcodedModelGenerator [output-dir]\n\n");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void generate() throws IOException, TemplateException {
        Configuration cfg = CodeGen.getFreemarkerConfiguration();
        generateInitializer(cfg);
    }

    private void generateInitializer(Configuration config) throws IOException, TemplateException {
        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put("prefixes", new TreeMap<>(Qudt.getPrefixesMap()));
        templateVars.put("quantityKinds", new TreeMap<>(Qudt.getQuantityKindsMap()));
        templateVars.put("units", new TreeMap<>(Qudt.getUnitsMap()));
        templateVars.put("systemsOfUnits", new TreeMap<>(Qudt.getSystemsOfUnitsMap()));
        generateJavaFile(config, templateVars);
    }

    private void generateJavaFile(Configuration config, Map<String, Object> templateVars)
            throws IOException, TemplateException {
        RdfOps.message("Generating " + FILENAME);
        File packageFile =
                new File(outputDir + "/" + DESTINATION_PACKAGE.replaceAll(Pattern.quote("."), "/"));
        if (!packageFile.exists()) {
            if (!packageFile.mkdirs()) {
                throw new IOException(
                        "Could not create output dir " + packageFile.getAbsolutePath());
            }
        }
        RdfOps.message("output dir: " + packageFile.getAbsolutePath());
        templateVars.put("package", DESTINATION_PACKAGE);
        File outFile = new File(packageFile, FILENAME);
        CodeGen.generateFileFromTemplate(config, TEMPLATE_FILE, templateVars, outFile);
    }
}
