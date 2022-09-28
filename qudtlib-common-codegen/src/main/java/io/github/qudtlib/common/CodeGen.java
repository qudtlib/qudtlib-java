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

    public static SafeStringMapper javaConstantMapper() {
        return new SafeStringMapper(javaConstantNameMapper);
    }

    static Function<String, String> javaConstantNameMapper =
            constName -> {
                Pattern startPattern = Pattern.compile("^[$€a-zA-Z_]");
                if (!startPattern.matcher(constName).lookingAt()) {
                    return "_" + constName;
                }
                return constName;
            };
}
