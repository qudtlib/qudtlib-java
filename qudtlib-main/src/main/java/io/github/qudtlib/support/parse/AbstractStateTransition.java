package io.github.qudtlib.support.parse;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class AbstractStateTransition implements StateTransition {
    private final String name;
    private final Pattern pattern;

    public AbstractStateTransition(String name, Pattern pattern) {
        this.name = name;
        this.pattern = pattern;
    }

    @Override
    public boolean mayMatchLater(String string) {
        return pattern.matcher(string).matches();
    }

    @Override
    public final List<State> apply(State state) {
        Matcher m = getMatcher(state);
        String matchedToken = null;
        if (!m.matches()) {
            return List.of();
        }
        matchedToken = m.group();
        List<State> result =
                parse(
                        state,
                        matchedToken,
                        new ArrayList<>(getAllowedNextTransitions()),
                        new ArrayList<>(getRequiredClosingTransitions()));
        return result;
    }

    protected abstract List<State> parse(
            State state,
            String matchedToken,
            List<StateTransition> nextTransitions,
            List<StateTransition> requiredClosingTransitions);

    /**
     * Used to enforce ')' when '(' is encountered
     *
     * @return
     */
    protected abstract List<StateTransition> getRequiredClosingTransitions();

    protected abstract List<StateTransition> getAllowedNextTransitions();

    private Matcher getMatcher(State state) {
        return getPattern().matcher(state.getMatchableInput());
    }

    private Pattern getPattern() {
        return this.pattern;
    }

    protected void pushTransitions(
            Deque<List<StateTransition>> transitionStack, List<StateTransition> transitions) {
        if (transitions != null && !transitions.isEmpty()) {
            transitionStack.push(transitions);
        }
    }

    public String toString() {
        return name;
    }
}
