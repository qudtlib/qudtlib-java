package io.github.qudtlib;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import io.github.qudtlib.common.CodeGen;
import io.github.qudtlib.common.safenames.SafeStringMapper;
import io.github.qudtlib.constgen.Constant;
import io.github.qudtlib.model.*;
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
        templateVars.put("systemsOfUnits", new TreeMap<>(Qudt.getSystemsOfUnitsMap()));
        Set<Constant> unitConstants =
                Arrays.stream(Units.class.getDeclaredFields())
                        .map(
                                f -> {
                                    String name = f.getName();
                                    try {
                                        Unit unit = (Unit) f.get(Unit.class);
                                        return CodeGen.makeConstant(
                                                unit.getLabels(),
                                                unit.getIri(),
                                                "Unit",
                                                unit.getSymbol().orElse(null),
                                                name,
                                                this.constantNameMapper);
                                    } catch (IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                        .collect(Collectors.toSet());
        Set<Constant> quantityKindConstants =
                Arrays.stream(QuantityKinds.class.getDeclaredFields())
                        .map(
                                f -> {
                                    String name = f.getName();
                                    try {
                                        QuantityKind quantityKind =
                                                (QuantityKind) f.get(QuantityKind.class);
                                        return CodeGen.makeConstant(
                                                quantityKind.getLabels(),
                                                quantityKind.getIri(),
                                                "QuantityKind",
                                                quantityKind.getSymbol().orElse(null),
                                                name,
                                                this.constantNameMapper);
                                    } catch (IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                        .collect(Collectors.toSet());
        Set<Constant> prefixConstants =
                Arrays.stream(Prefixes.class.getDeclaredFields())
                        .map(
                                f -> {
                                    String name = f.getName();
                                    try {
                                        Prefix prefix = (Prefix) f.get(Prefix.class);
                                        return CodeGen.makeConstant(
                                                prefix.getLabels(),
                                                prefix.getIri(),
                                                "Prefix",
                                                prefix.getSymbol(),
                                                name,
                                                this.constantNameMapper);
                                    } catch (IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                        .collect(Collectors.toSet());
        Set<Constant> systemOfUnitConstants =
                Arrays.stream(SystemsOfUnits.class.getDeclaredFields())
                        .map(
                                f -> {
                                    String name = f.getName();
                                    try {
                                        SystemOfUnits systemOfUnits =
                                                (SystemOfUnits) f.get(SystemOfUnits.class);
                                        return CodeGen.makeConstant(
                                                systemOfUnits.getLabels(),
                                                systemOfUnits.getIri(),
                                                "SystemOfUnits",
                                                systemOfUnits.getAbbreviation().orElse(null),
                                                name,
                                                this.constantNameMapper);
                                    } catch (IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                        .collect(Collectors.toSet());
        templateVars.put("unitConstants", unitConstants);
        templateVars.put("quantityKindConstants", quantityKindConstants);
        templateVars.put("prefixConstants", prefixConstants);
        templateVars.put("systemOfUnitConstants", systemOfUnitConstants);
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
