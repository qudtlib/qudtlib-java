package io.github.qudtlib.common;

import freemarker.core.Environment;
import freemarker.core.TemplateNumberFormat;
import freemarker.core.TemplateNumberFormatFactory;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import java.math.BigDecimal;
import java.util.Locale;

public class BigDecimalFormatterFactory extends TemplateNumberFormatFactory {
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
