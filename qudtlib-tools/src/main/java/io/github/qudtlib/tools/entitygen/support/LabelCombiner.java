package io.github.qudtlib.tools.entitygen.support;

import io.github.qudtlib.model.FactorUnit;
import io.github.qudtlib.model.FactorUnits;
import io.github.qudtlib.model.LangString;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LabelCombiner {
    private static final Map<String, Map<Integer, StringFunction>> exponents =
            Map.ofEntries(
                    Map.entry(
                            "en",
                            Map.of(
                                    1,
                                    asIs(),
                                    2,
                                    prepend("square "),
                                    3,
                                    prepend("cubic "),
                                    4,
                                    prepend("quartic "),
                                    5,
                                    prepend("quintic "),
                                    6,
                                    prepend("sextic "))),
                    Map.entry(
                            "de",
                            Map.of(
                                    1,
                                    asIs(),
                                    2,
                                    prepend("quadrat"),
                                    3,
                                    prepend("kubik"),
                                    4,
                                    prepend("biquadrat"),
                                    5,
                                    append(" hoch 5"),
                                    6,
                                    append(" hoch 6"))),
                    Map.entry(
                            "sl",
                            Map.of(1, asIs(), 2, prepend("kvadratni "), 3, prepend("kubični "))),
                    Map.entry("ms", Map.of(1, asIs(), 2, append(" persegi "), 3, append(" kubik"))),
                    Map.entry(
                            "pl",
                            Map.of(1, asIs(), 2, prepend(" kwadratowy"), 3, append(" sześcienny"))),
                    Map.entry("tr", Map.of(1, asIs(), 2, append("kare"), 3, append("küp"))),
                    Map.entry(
                            "es", Map.of(1, asIs(), 2, append(" cuadrado"), 3, append(" cúbico"))),
                    Map.entry(
                            "pt", Map.of(1, asIs(), 2, append(" quadrado"), 3, append(" cúbico"))),
                    Map.entry("it", Map.of(1, asIs(), 2, append(" quadrato"), 3, append(" cubo"))),
                    Map.entry("ro", Map.of(1, asIs(), 2, append(" pătrat"), 3, append(" cub"))),
                    Map.entry(
                            "la",
                            Map.of(1, asIs(), 2, append(" quadratum"), 3, append(" cubicum"))),
                    Map.entry("fr", Map.of(1, asIs(), 2, append(" carré"), 3, append(" cube"))),
                    Map.entry("hu", Map.of(1, asIs(), 2, prepend("négyzet"), 3, prepend("köb"))),
                    Map.entry(
                            "cs",
                            Map.of(1, asIs(), 2, prepend("čtvereční "), 3, prepend(" krychlový"))),
                    Map.entry(
                            "el",
                            Map.of(1, asIs(), 2, append("τετραγωνικό "), 3, prepend("κυβικό "))),
                    Map.entry(
                            "bg",
                            Map.of(1, asIs(), 2, append("квадратен "), 3, prepend("кубичен "))),
                    Map.entry(
                            "ru",
                            Map.of(
                                    1,
                                    asIs(),
                                    2,
                                    append("квадратный "),
                                    3,
                                    prepend("кубический "))));

    private static final Map<String, Function<StringPair, String>> multipliers =
            Map.ofEntries(
                    Map.entry("en", join(" ").andThen(lowercase())),
                    Map.entry("de", join(" ").andThen(capitalizeFirstLetterOnly())),
                    Map.entry("en-us", both(capitalizeFirstLetterOnly()).andThen(join(" "))),
                    Map.entry("it", join(" per ").andThen(lowercase())),
                    Map.entry("sl", join(" ").andThen(lowercase())),
                    Map.entry("ms", join(" ").andThen(lowercase())),
                    Map.entry("tr", join(" ").andThen(lowercase())),
                    Map.entry("es", join(" ").andThen(lowercase())),
                    Map.entry("cs", join(" ").andThen(lowercase())),
                    Map.entry("pt", join(" ").andThen(lowercase())),
                    Map.entry("fr", join("-").andThen(lowercase())),
                    Map.entry("ro", join("-").andThen(lowercase())),
                    Map.entry("pl", join("o").andThen(lowercase())),
                    Map.entry("ru", join("-").andThen(lowercase())));

    private static final Map<String, Function<StringPair, String>> dividers =
            Map.ofEntries(
                    Map.entry("en", joinWithFallback(" per ", null, "reciprocal ", null)),
                    Map.entry("de", join(" je ")),
                    Map.entry("en-us", join(" Per ")),
                    Map.entry("it", join(" al ")),
                    Map.entry("sl", join(" na ")),
                    Map.entry("ms", join(" per ")),
                    Map.entry("tr", join(" per ")),
                    Map.entry("es", join(" por")),
                    Map.entry("cs", join(" na ")),
                    Map.entry("fr", join(" par ")),
                    Map.entry("ro", join(" pe ")),
                    Map.entry("pt", join(" por ")),
                    Map.entry("pl", join(" na ")),
                    Map.entry("ru", join(" на ")));
    private static final String DEFAULT_LANGUAGE_TAG = "en";
    private static String NO_LANG_TAG = "no-lang-tag";

    public static List<LangString> forFactorUnits(FactorUnits factorUnits) {
        List<String> langTags =
                factorUnits.getFactorUnits().get(0).getUnit().getLabels().stream()
                        .map(l -> l.getLanguageTag())
                        .filter(
                                langTag ->
                                        factorUnits.getFactorUnits().stream()
                                                .allMatch(
                                                        otherUnit ->
                                                                otherUnit
                                                                        .getUnit()
                                                                        .getLabels()
                                                                        .stream()
                                                                        .anyMatch(
                                                                                l ->
                                                                                        Objects
                                                                                                .equals(
                                                                                                        l
                                                                                                                .getLanguageTag(),
                                                                                                        langTag))))
                        .map(o -> o.orElse(NO_LANG_TAG))
                        .collect(Collectors.toList());
        return langTags.stream()
                .map(
                        langTag -> {
                            List<FactorUnit> numerator =
                                    factorUnits.getFactorUnits().stream()
                                            .filter(fu -> fu.getExponent() > 0)
                                            .collect(Collectors.toList());

                            List<FactorUnit> denominator =
                                    factorUnits.getFactorUnits().stream()
                                            .filter(fu -> fu.getExponent() < 0)
                                            .collect(Collectors.toList());
                            List<String> numeratorStr =
                                    numerator.stream()
                                            .map(fu -> factorUnitToString(fu, langTag))
                                            .filter(Optional::isPresent)
                                            .map(Optional::get)
                                            .collect(Collectors.toList());
                            List<String> denominatorStr =
                                    denominator.stream()
                                            .map(fu -> factorUnitToString(fu, langTag))
                                            .filter(Optional::isPresent)
                                            .map(Optional::get)
                                            .collect(Collectors.toList());
                            if (numeratorStr.size() + denominatorStr.size()
                                    != factorUnits.getFactorUnits().size()) {
                                // one of the configurations for exponents is missing for our
                                // current language, omit
                                return Optional.ofNullable((LangString) null);
                            }
                            Function<StringPair, String> div = dividers.get(langTag);
                            Function<StringPair, String> mul = multipliers.get(langTag);
                            if (mul == null || div == null) {
                                return Optional.ofNullable((LangString) null);
                            }
                            return Optional.of(
                                    new LangString(
                                            div.apply(
                                                    new StringPair(
                                                            numeratorStr.stream()
                                                                    .reduce(
                                                                            (left, right) ->
                                                                                    mul.apply(
                                                                                            new StringPair(
                                                                                                    left,
                                                                                                    right)))
                                                                    .orElse(null),
                                                            denominatorStr.stream()
                                                                    .reduce(
                                                                            (left, right) ->
                                                                                    mul.apply(
                                                                                            new StringPair(
                                                                                                    left,
                                                                                                    right)))
                                                                    .orElse(null))),
                                            langTag));
                        })
                .filter(Optional::isPresent)
                .map(opt -> opt.get())
                .collect(Collectors.toList());
    }

    private static Optional<String> factorUnitToString(FactorUnit factorUnit, String languageTag) {
        var config = exponents.get(languageTag);
        if (config == null) {
            return Optional.empty();
        }
        var fun = config.get(Math.abs(factorUnit.getExponent()));
        if (fun == null) {
            return Optional.empty();
        }
        String unit =
                factorUnit
                        .getUnit()
                        .getLabelForLanguageTag(languageTag, DEFAULT_LANGUAGE_TAG, true)
                        .orElse("");
        return Optional.of(fun.apply(unit));
    }

    private static String capitalize(String firstLabel) {
        return firstLabel.substring(0, 1).toUpperCase() + firstLabel.substring(1).toLowerCase();
    }

    private static interface StringPairFunction extends Function<StringPair, StringPair> {}

    private static interface StringPairCombinationFunction extends Function<StringPair, String> {}
    ;

    private static interface StringFunction extends Function<String, String> {}
    ;

    private static final StringPairFunction flip() {
        return stringPair -> new StringPair(stringPair.second, stringPair.first);
    }
    ;

    private static final StringPairFunction both(StringFunction fun) {
        return stringPair ->
                new StringPair(fun.apply(stringPair.first), fun.apply(stringPair.second));
    }

    private static StringPairCombinationFunction join(String joinString) {
        return stringPair -> stringPair.join(joinString);
    }

    private static StringPairCombinationFunction joinWithFallback(
            String joinString,
            String suffixIfSecondNull,
            String prefixIfFirstNull,
            String resultIfBothNull) {
        return stringPair ->
                stringPair.joinWithFallback(
                        joinString, suffixIfSecondNull, prefixIfFirstNull, resultIfBothNull);
    }

    private static StringFunction prepend(String prefix) {
        return s -> safeString(prefix) + safeString(s);
    }

    private static StringFunction append(String suffix) {
        return s -> safeString(s) + safeString(suffix);
    }

    private static String safeString(String prefix) {
        return prefix == null ? "" : prefix;
    }

    private static final StringFunction capitalizeFirstLetterOnly() {
        return s -> {
            String[] parts = s.split(" ");
            return Arrays.stream(parts)
                    .map(p -> p.substring(0, 1).toUpperCase() + p.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
        };
    }
    ;

    private static final StringFunction lowercase() {
        return s -> s.toLowerCase();
    }

    private static final StringFunction singleWhitespace() {
        return s -> s.replaceAll("\\s+", " ");
    }
    ;

    private static final StringFunction asIs() {
        return s -> s;
    }

    private static class StringPair {
        private final String first;
        private final String second;

        public StringPair(String first, String second) {
            this.first = first;
            this.second = second;
        }

        public String join(String joinString) {
            if (first == null) {
                return second;
            }
            if (second == null) {
                return first;
            }
            return first + safeString(joinString) + second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StringPair)) return false;
            StringPair that = (StringPair) o;
            return Objects.equals(first, that.first) && Objects.equals(second, that.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }

        public String joinWithFallback(
                String joinString,
                String suffixIfSecondNull,
                String prefixIfFirstNull,
                String resultIfBothNull) {
            if (first == null) {
                if (second == null) {
                    return safeString(resultIfBothNull);
                } else {
                    return safeString(prefixIfFirstNull) + second;
                }
            } else if (second == null) {
                return safeString(prefixIfFirstNull) + second;
            }
            return first + safeString(joinString) + second;
        }
    }
}
