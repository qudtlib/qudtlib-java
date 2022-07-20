package io.github.qudtlib.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents an RDF langString - a {@link String} with a language tag, such as "Unit"@en.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public class LangString {
    private final String string;
    private final String languageTag;

    public LangString(String string) {
        this(string, null);
    }

    public LangString(String string, String languageTag) {
        Objects.requireNonNull(string);
        this.string = string;
        this.languageTag = languageTag;
    }

    public String getString() {
        return string;
    }

    public Optional<String> getLanguageTag() {
        return Optional.ofNullable(languageTag);
    }

    @Override
    public String toString() {
        return "'" + string + "'" + (languageTag == null ? "" : "@" + languageTag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LangString that = (LangString) o;
        return string.equals(that.string) && Objects.equals(languageTag, that.languageTag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(string, languageTag);
    }
}
