package io.github.qudtlib.tools.contribute.support;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class FormattingHelper {

    private static final DecimalFormat decimalFormat;
    private static final DecimalFormatSymbols symbols;

    static {
        symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        decimalFormat =
                new DecimalFormat(
                        "0.0#####################################################################################################################################################",
                        symbols);
    }

    public static String format(BigDecimal bigDecimal) {
        return decimalFormat.format(bigDecimal);
    }
}
