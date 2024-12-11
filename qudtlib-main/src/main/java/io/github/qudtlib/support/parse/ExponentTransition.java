package io.github.qudtlib.support.parse;

import java.util.*;
import java.util.regex.Pattern;

class ExponentTransition extends AbstractStateTransition {
    private static final Pattern P_EXPONENT = Pattern.compile("\\^?[-⁻]?[\\d¹²³⁴⁵⁶⁷⁸⁹⁰]");
    private static final Pattern P_PARTIAL = Pattern.compile("\\^?[-⁻]?");

    public ExponentTransition() {
        super("Exponent", P_EXPONENT);
    }

    @Override
    public boolean mayMatchLater(String string) {
        return P_PARTIAL.matcher(string).matches() || P_EXPONENT.matcher(string).matches();
    }

    @Override
    protected List<State> parse(
            State state,
            String matchedToken,
            List<StateTransition> nextTransitions,
            List<StateTransition> requiredClosingTransitions) {
        String sanitizedExponent =
                matchedToken
                        .replaceFirst("\\^", "")
                        .replaceAll("¹", "1")
                        .replaceAll("²", "2")
                        .replaceAll("³", "3")
                        .replaceAll("⁴", "4")
                        .replaceAll("⁵", "5")
                        .replaceAll("⁶", "6")
                        .replaceAll("⁷", "7")
                        .replaceAll("⁸", "8")
                        .replaceAll("⁹", "9")
                        .replaceAll("⁰", "0")
                        .replaceAll("⁻", "-");
        int parsedExponent = Integer.valueOf(sanitizedExponent);
        if (state.isDividerSeen() && parsedExponent < 0) {
            return List.of();
        }
        if (parsedExponent == 0) {
            return List.of();
        }
        List<ParsedUnit> parsedUnitsBase = state.getParsedUnits();
        if (parsedUnitsBase.isEmpty()) {
            return List.of();
        }
        ParsedUnit toModify = parsedUnitsBase.remove(parsedUnitsBase.size() - 1);
        ParsedUnit modified = toModify.pow(parsedExponent, matchedToken);
        parsedUnitsBase.add(modified);
        Deque<List<StateTransition>> transitionStack =
                new ArrayDeque<>(state.getStateTransitionStack());
        if (!state.isAtEnd()
                && !state.isDividerSeen()
                && parsedExponent > 0
                && !state.isNegativeExponentSeen()) {
            nextTransitions.add(StateTransition.DIVIDER);
        }
        pushTransitions(transitionStack, requiredClosingTransitions);
        pushTransitions(transitionStack, nextTransitions);
        return List.of(
                new State(
                        state.remainingInputForNextState(),
                        null,
                        parsedUnitsBase,
                        state.isDividerSeen(),
                        parsedExponent < 0,
                        transitionStack));
    }

    @Override
    protected List<StateTransition> getRequiredClosingTransitions() {
        return List.of();
    }

    @Override
    protected List<StateTransition> getAllowedNextTransitions() {
        return List.of(
                StateTransition.UNIT,
                StateTransition.DOT,
                StateTransition.WHITESPACE,
                StateTransition.OPENING_BRACKET);
    }
}
