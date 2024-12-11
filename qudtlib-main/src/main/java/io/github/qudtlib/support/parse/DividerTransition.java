package io.github.qudtlib.support.parse;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

class DividerTransition extends AbstractStateTransition {

    private static final Pattern P_DIVIDER =
            Pattern.compile("(/|per|pro|je|par|por)", Pattern.CASE_INSENSITIVE);

    public DividerTransition() {
        super("Divider", P_DIVIDER);
    }

    @Override
    public boolean mayMatchLater(String string) {
        return List.of("/", "per", "pro", "je", "par", "por").stream()
                .anyMatch(s -> s.toUpperCase().startsWith(string.toUpperCase()));
    }

    @Override
    protected List<State> parse(
            State state,
            String matchedToken,
            List<StateTransition> nextTransitions,
            List<StateTransition> requiredClosingTransitions) {
        if (state.isDividerSeen()) {
            throw new IllegalStateException(
                    String.format("Already seen divider, not allowed twice! State: %s", state));
        }
        Deque<List<StateTransition>> transitionStack =
                new ArrayDeque<>(state.getStateTransitionStack());
        pushTransitions(transitionStack, requiredClosingTransitions);
        pushTransitions(transitionStack, nextTransitions);
        return List.of(state.withDividerSeen(state.remainingInputForNextState(), transitionStack));
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
