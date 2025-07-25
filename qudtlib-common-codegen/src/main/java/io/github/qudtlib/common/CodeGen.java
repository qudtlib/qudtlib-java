package io.github.qudtlib.common;

import freemarker.core.Environment;
import freemarker.template.*;
import io.github.qudtlib.common.safenames.SafeStringMapper;
import io.github.qudtlib.constgen.Constant;
import io.github.qudtlib.model.LangString;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

public abstract class CodeGen {

    public static Configuration getFreemarkerConfiguration() {
        return getFreemarkerConfiguration(Thread.currentThread().getContextClassLoader());
    }

    public static Configuration getFreemarkerConfiguration(ClassLoader classLoaderForTemplate) {
        Objects.requireNonNull(classLoaderForTemplate);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setClassLoaderForTemplateLoading(classLoaderForTemplate, "/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setCustomNumberFormats(Map.of("toString", new BigDecimalFormatterFactory()));
        return cfg;
    }

    public static void generateFileFromTemplate(
            Configuration config,
            String templateFileClasspathUrl,
            Map<String, Object> templateVars,
            File outFile)
            throws IOException, TemplateException {
        Template template = config.getTemplate(templateFileClasspathUrl);
        FileWriter out = new FileWriter(outFile, StandardCharsets.UTF_8);
        Environment env = template.createProcessingEnvironment(templateVars, out);
        env.setOutputEncoding(StandardCharsets.UTF_8.toString());
        env.process();
    }

    public static Constant makeConstant(
            Set<LangString> labels,
            String iri,
            String typeName,
            String symbol,
            String constantName,
            SafeStringMapper constantNameMapper) {
        String label = labels.stream().findFirst().map(LangString::getString).orElse("[no label]");
        String iriLocalName = iri.replaceAll("^.+[/|#]", "");
        String codeConstantName = constantNameMapper.applyMapping(constantName);
        String valueFactory =
                typeName.substring(0, 1).toLowerCase()
                        + typeName.substring(1)
                        + "FromLocalnameRequired";
        return new Constant(
                codeConstantName, iriLocalName, label, iri, typeName, symbol, valueFactory);
    }

    public static SafeStringMapper javaConstantMapper() {
        return new SafeStringMapper(javaConstantNameMapper);
    }

    static final Function<String, String> javaConstantNameMapper =
            constName -> {
                Pattern startPattern = Pattern.compile("^[$€a-zA-Z_]");
                if (!startPattern.matcher(constName).lookingAt()) {
                    constName = "_" + constName;
                }
                constName = constName.replaceAll("-", "__").replaceAll("\\.", "pt");
                return constName;
            };
}
