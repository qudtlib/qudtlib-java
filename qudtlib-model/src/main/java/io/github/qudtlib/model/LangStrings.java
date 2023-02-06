package io.github.qudtlib.model;

import static java.util.stream.Collectors.*;

import java.util.*;

public class LangStrings {
    private static final String KEY_NO_TAG = "no-language-tag";
    private final Map<String, Set<LangString>> langStrings;

    public LangStrings(Collection<LangString> langStrings) {
        this.langStrings =
                Collections.unmodifiableMap(
                        langStrings.stream()
                                .collect(
                                        groupingBy(
                                                l -> l.getLanguageTag().orElse(KEY_NO_TAG),
                                                toSet())));
    }

    public Optional<String> getStringForLanguageTag(
            String language, String fallbackLanguage, boolean allowAnyIfNoMatch) {
        return getLangStringForLanguageTag(language, fallbackLanguage, allowAnyIfNoMatch)
                .map(ls -> ls.getString());
    }

    public Optional<LangString> getLangStringForLanguageTag(
            String language, String fallbackLanguage, boolean allowAnyIfNoMatch) {
        if (language == null) {
            return getAnyLangString();
        }
        Optional<LangString> result = getAnyLangStringForLanguageTag(language);
        if (result.isPresent()) {
            return result;
        }
        if (fallbackLanguage != null) {
            result = getAnyLangStringForLanguageTag(fallbackLanguage);
            if (result != null) {
                return result;
            }
        }
        if (allowAnyIfNoMatch) {
            return getAnyLangString();
        }
        return Optional.empty();
    }

    private Optional<LangString> getAnyLangStringForLanguageTag(String language) {
        return Optional.ofNullable(this.langStrings.get(language))
                .map(s -> s.stream().findFirst().orElse(null));
    }

    private Optional<LangString> getAnyLangString() {
        return this.langStrings.values().stream()
                .findFirst()
                .map(s -> s.stream().findFirst().orElse(null));
    }

    public boolean containsStringForLanguageTag(String languageTag) {
        return this.langStrings.containsKey(languageTag);
    }

    public Set<LangString> getAll() {
        return this.langStrings.values().stream().flatMap(Collection::stream).collect(toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LangStrings that = (LangStrings) o;
        return langStrings.equals(that.langStrings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(langStrings);
    }

    public boolean containsString(String label) {
        return this.langStrings.values().stream()
                .flatMap(Collection::stream)
                .anyMatch(s -> s.getString().equals(label));
    }
}
