package io.github.qudtlib.support.parse;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

class ClosingBracketTransition extends AbstractStateTransition {
    private static final Pattern P_CLOSING_BRACKET = Pattern.compile("\\)");

    public ClosingBracketTransition() {
        super("ClosingBracket", P_CLOSING_BRACKET);
    }

    @Override
    public boolean mayMatchLater(String string) {
        return P_CLOSING_BRACKET.matcher(string).matches();
    }

    @Override
    protected List<State> parse(
            State state,
            String matchedToken,
            List<StateTransition> nextTransitions,
            List<StateTransition> requiredClosingTransitions) {
        Deque<List<StateTransition>> transitionStack =
                new ArrayDeque<>(state.getStateTransitionStack());
        if (!state.isDividerSeen() && !state.isNegativeExponentSeen()) {
            nextTransitions.add(StateTransition.DIVIDER);
        }
        pushTransitions(transitionStack, requiredClosingTransitions);
        pushTransitions(transitionStack, nextTransitions);
        return List.of(
                new State(
                        state.remainingInputForNextState(),
                        null,
                        state.getParsedUnits(),
                        state.isDividerSeen(),
                        state.isNegativeExponentSeen(),
                        transitionStack));
    }

    @Override
    protected List<StateTransition> getRequiredClosingTransitions() {
        return List.of();
    }

    @Override
    protected List<StateTransition> getAllowedNextTransitions() {
        return List.of(
                StateTransition.UNIT, StateTransition.WHITESPACE, StateTransition.OPENING_BRACKET);
    }
}
