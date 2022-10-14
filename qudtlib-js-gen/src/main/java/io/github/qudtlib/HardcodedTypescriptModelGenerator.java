package io.github.qudtlib;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import io.github.qudtlib.common.CodeGen;
import io.github.qudtlib.common.safenames.SafeStringMapper;
import io.github.qudtlib.constgen.Constant;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates a Typescript package that instantiates all QUDT individuals and relationships needed
 * for QUDTLib from hardcoded data, i.e. without the need to process RDF.
 *
 * <p>This generator accesses the QUDT model through the 'hardcoded' QUDTLib implementation.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class HardcodedTypescriptModelGenerator {
    private final Path outputDir;
    // output
    private static final String FILENAME = "units.ts";
    // template
    private static final String TEMPLATE_FILE = "template/units.ts.ftl";

    public HardcodedTypescriptModelGenerator(Path outputDir) {
        this.outputDir = outputDir;
    }

    private final SafeStringMapper constantNameMapper = CodeGen.javaConstantMapper();

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                throw new IllegalArgumentException("missing argument");
            }
            if (args.length > 1) {
                throw new IllegalArgumentException(" too many arguments");
            }
            String outputDir = args[0];
            HardcodedTypescriptModelGenerator generator =
                    new HardcodedTypescriptModelGenerator(Path.of(outputDir));
            generator.generate();
        } catch (Exception e) {
            System.err.println("\n\n\tusage: HardcodedTypescriptModelGenerator [output-dir]\n\n");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void generate() throws IOException, TemplateException {
        Configuration cfg = CodeGen.getFreemarkerConfiguration();
        generate(cfg);
    }

    private void generate(Configuration config) throws IOException, TemplateException {
        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put("prefixes", new TreeMap<>(Qudt.getPrefixesMap()));
        templateVars.put("quantityKinds", new TreeMap<>(Qudt.getQuantityKindsMap()));
        templateVars.put("units", new TreeMap<>(Qudt.getUnitsMap()));
        Set<Constant> unitConstants =
                Qudt.getUnitsMap().values().stream()
                        .map(
                                u ->
                                        CodeGen.makeConstant(
                                                u.getLabels(), u.getIri(), this.constantNameMapper))
                        .collect(Collectors.toSet());
        Set<Constant> quantityKindConstants =
                Qudt.getQuantityKindsMap().values().stream()
                        .map(
                                q ->
                                        CodeGen.makeConstant(
                                                q.getLabels(), q.getIri(), this.constantNameMapper))
                        .collect(Collectors.toSet());
        Set<Constant> prefixConstants =
                Qudt.getPrefixesMap().values().stream()
                        .map(
                                p ->
                                        CodeGen.makeConstant(
                                                p.getLabels(), p.getIri(), this.constantNameMapper))
                        .collect(Collectors.toSet());
        templateVars.put("unitConstants", unitConstants);
        templateVars.put("quantityKindConstants", quantityKindConstants);
        templateVars.put("prefixConstants", prefixConstants);
        generateTypescriptFile(config, templateVars);
    }

    private void generateTypescriptFile(Configuration config, Map<String, Object> templateVars)
            throws IOException, TemplateException {
        System.out.println("Generating " + FILENAME);
        File packageFile = outputDir.toFile();
        if (!packageFile.exists()) {
            if (!packageFile.mkdirs()) {
                throw new IOException(
                        "Could not create output dir " + packageFile.getAbsolutePath());
            }
        }
        System.out.println("output dir: " + packageFile.getAbsolutePath());
        File outFile = new File(packageFile, FILENAME);
        CodeGen.generateFileFromTemplate(config, TEMPLATE_FILE, templateVars, outFile);
    }
}
