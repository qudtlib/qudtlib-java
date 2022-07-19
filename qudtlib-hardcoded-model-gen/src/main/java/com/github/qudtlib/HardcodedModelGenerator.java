package com.github.qudtlib;

import com.github.qudlib.common.RdfOps;
import freemarker.core.Environment;
import freemarker.core.TemplateNumberFormat;
import freemarker.core.TemplateNumberFormatFactory;
import freemarker.template.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
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
    private static final String DESTINATION_PACKAGE = "com.github.qudtlib.model";
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
        Configuration cfg = getFreemarkerConfiguration();
        generateInitializer(cfg);
    }

    private Configuration getFreemarkerConfiguration() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setClassLoaderForTemplateLoading(HardcodedModelGenerator.class.getClassLoader(), "/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setCustomNumberFormats(Map.of("toString", new BigDecimalFormatterFactory()));
        return cfg;
    }

    private void generateInitializer(Configuration config) throws IOException, TemplateException {
        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put("prefixes", new TreeMap<>(Qudt.getPrefixesMap()));
        templateVars.put("quantityKinds", new TreeMap<>(Qudt.getQuantityKindsMap()));
        templateVars.put("units", new TreeMap<>(Qudt.getUnitsMap()));
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
        Template template = config.getTemplate(TEMPLATE_FILE);
        FileWriter out = new FileWriter(new File(packageFile, FILENAME), StandardCharsets.UTF_8);
        Environment env = template.createProcessingEnvironment(templateVars, out);
        env.setOutputEncoding(StandardCharsets.UTF_8.toString());
        env.process();
    }

    private static class BigDecimalFormatterFactory extends TemplateNumberFormatFactory {
        @Override
        public TemplateNumberFormat get(String s, Locale locale, Environment environment) {
            return new TemplateNumberFormat() {
                @Override
                public String formatToPlainText(TemplateNumberModel templateNumberModel)
                        throws TemplateModelException {
                    Number num = templateNumberModel.getAsNumber();
                    if (!(num instanceof BigDecimal)) {
                        throw new IllegalArgumentException(
                                "This formatter can only be used with BigDecimals but was asked to format a "
                                        + num.getClass());
                    }
                    BigDecimal bd = (BigDecimal) templateNumberModel.getAsNumber();
                    return bd.toString();
                }

                @Override
                public boolean isLocaleBound() {
                    return false;
                }

                @Override
                public String getDescription() {
                    return "Number format for BigDecimal using BigDecimal.toString()";
                }
            };
        }
    }
}
