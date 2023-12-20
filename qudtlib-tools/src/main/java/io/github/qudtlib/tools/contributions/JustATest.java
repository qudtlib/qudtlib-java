package io.github.qudtlib.tools.contributions;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class JustATest {
    public static void main(String[] args) {
        BigDecimal bd = new BigDecimal("0.00000000010000000");
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        DecimalFormat df =
                new DecimalFormat("0.00000000000000000000", new DecimalFormatSymbols(Locale.US));
        System.out.println(df.format(bd));
    }
}
