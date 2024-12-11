package io.github.qudtlib.support.parse;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

class DotTransition extends AbstractStateTransition {
    private static final Pattern P_DOT = Pattern.compile("[·*.]");

    public DotTransition() {
        super("Dot", P_DOT);
    }

    @Override
    protected List<State> parse(
            State state,
            String matchedToken,
            List<StateTransition> nextTransitions,
            List<StateTransition> requiredClosingTransitions) {
        Deque<List<StateTransition>> transitionStack =
                new ArrayDeque<>(state.getStateTransitionStack());
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
