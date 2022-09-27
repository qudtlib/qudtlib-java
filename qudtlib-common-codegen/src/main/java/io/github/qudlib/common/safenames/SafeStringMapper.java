package io.github.qudlib.common.safenames;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Applies the provided <b>deterministic</b> </b><code>mappingFunction</code> provided in the
 * constructor, keeping track of the mappings produced. If two different inputs result in the same
 * output, a {@link NameCollisionException} is thrown.
 */
public class SafeStringMapper {
    private final Map<String, String> outputToInput = new ConcurrentHashMap<>();
    private final Function<String, String> mappingFunction;

    public SafeStringMapper(Function<String, String> mappingFunction) {
        this.mappingFunction = mappingFunction;
    }

    /**
     * Applies the specified mapping, throwing an exception if a previously produced output is
     * reproduced with a different input.
     *
     * @param input the value to map safely
     * @return the mapped value
     */
    public String applyMapping(String input) {
        Objects.requireNonNull(input);
        final String output = mappingFunction.apply(input);
        outputToInput.merge(
                output,
                input,
                (previousInput, currentInput) -> {
                    if (previousInput.equals(currentInput)) {
                        return input;
                    }
                    throw new NameCollisionException(input, previousInput, output);
                });
        return output;
    }
}
