package io.github.qudtlib.support.parse;

import java.util.*;
import java.util.stream.Collectors;

class State {
    private static int idCounter = 1;
    private final int id = State.idCounter++;
    private final String remainingInput;
    private final String leftoverInput;
    private final List<ParsedUnit> parsedUnits;
    private final Deque<List<StateTransition>> stateTransitionStack;
    private final boolean dividerSeen;
    private final boolean negativeExponentSeen;

    public State(
            String remainingInput,
            String leftoverInput,
            List<ParsedUnit> parsedUnits,
            boolean dividerSeen,
            boolean negativeExponentSeen,
            Deque<List<StateTransition>> transitionStack) {
        this.remainingInput = remainingInput;
        this.leftoverInput = leftoverInput;
        this.stateTransitionStack = new ArrayDeque<>(transitionStack);
        this.dividerSeen = dividerSeen;
        this.negativeExponentSeen = negativeExponentSeen;
        this.parsedUnits = new ArrayList<>(parsedUnits);
    }

    public State(String input, StateTransition... stateTransitionStack) {
        this.remainingInput = input;
        this.leftoverInput = null;
        this.stateTransitionStack = new ArrayDeque<>();
        this.stateTransitionStack.push(Arrays.asList(stateTransitionStack));
        this.dividerSeen = false;
        this.negativeExponentSeen = false;
        this.parsedUnits = new ArrayList<>();
    }

    private int currentBadness() {
        return this.parsedUnits.size();
    }

    private int expectedWorstCaseBadness() {
        return this.remainingInput.length();
    }

    public int badness() {
        return this.currentBadness() + this.expectedWorstCaseBadness();
    }

    List<State> nextTransition() {
        //        System.out.println("state: " + this.toString());
        Deque<List<StateTransition>> stackCopy = new ArrayDeque<>(this.stateTransitionStack);
        List<State> resultingStates = new ArrayList<>();
        while (!this.stateTransitionStack.isEmpty()) {
            List<StateTransition> topLevelTransitions = this.stateTransitionStack.pop();
            for (StateTransition transition : topLevelTransitions) {
                List<State> resultingFromTransition = transition.apply(this);
                resultingStates.addAll(resultingFromTransition);
                //                System.out.println("transition: " + transition);
                //                    resultingFromTransition.stream()
                //                            .map(Object::toString)
                //                            .map(s -> " -> " + s)
                //                            .forEach(System.out::println);
                //                    if (resultingFromTransition.isEmpty()){
                ////                        System.out.println(" -> [no results]");
                // }
            }
        }
        if (!remainingInput.isEmpty()) {
            Optional<State> nopTransitionState = this.nopTransition(stackCopy);
            nopTransitionState.ifPresent(resultingStates::add);
            //            if (nopTransitionState.isPresent()) {
            //                System.out.println("transition: NOP");
            //                System.out.println(" -> " + nopTransitionState);
            //            }
        }

        return resultingStates;
    }

    private Optional<State> nopTransition(Deque<List<StateTransition>> transitionStack) {
        String nextInput = remainingInput.isEmpty() ? "" : remainingInput.substring(1);
        String firstChar = remainingInput.isEmpty() ? "" : remainingInput.substring(0, 1);
        String newLeftover = Optional.ofNullable(leftoverInput).orElse("") + firstChar;
        String nextMatchable = newLeftover + (nextInput.isEmpty() ? "" : nextInput.substring(0, 1));
        Deque<List<StateTransition>> newStack = new ArrayDeque<>(transitionStack);
        while (!newStack.isEmpty()) {
            List<StateTransition> transitions = newStack.pop();
            transitions =
                    transitions.stream()
                            .filter(t -> t.mayMatchLater(nextMatchable))
                            .collect(Collectors.toList());
            if (!transitions.isEmpty()) {
                newStack.push(transitions);
                break;
            }
        }
        if (newStack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(
                new State(
                        nextInput,
                        newLeftover,
                        parsedUnits,
                        dividerSeen,
                        negativeExponentSeen,
                        newStack));
    }

    boolean hasLeftoverInput() {
        return leftoverInput != null;
    }

    public String getRemainingInput() {
        return remainingInput;
    }

    public String getLeftoverInput() {
        return leftoverInput;
    }

    public String getMatchableInput() {
        return new StringBuilder()
                .append(this.leftoverInput == null ? "" : this.leftoverInput)
                .append(
                        this.remainingInput == null
                                ? ""
                                : this.remainingInput.isEmpty()
                                        ? ""
                                        : this.remainingInput.substring(0, 1))
                .toString();
    }

    public List<ParsedUnit> getParsedUnits() {
        return parsedUnits;
    }

    public Deque<List<StateTransition>> getStateTransitionStack() {
        return stateTransitionStack;
    }

    public boolean isDividerSeen() {
        return dividerSeen;
    }

    public boolean isNegativeExponentSeen() {
        return negativeExponentSeen;
    }

    @Override
    public String toString() {
        return "State{"
                + "id="
                + id
                + ", leftover='"
                + leftoverInput
                + '\''
                + ", remaining='"
                + remainingInput
                + '\''
                + ", dividerSeen="
                + dividerSeen
                + ", parsedUnits="
                + parsedUnits
                + ", transitions="
                + stateTransitionStack
                + '}';
    }

    public State withDividerSeen(String remainingInput, Deque<List<StateTransition>> transitions) {
        if (this.negativeExponentSeen) {
            throw new IllegalStateException(
                    "negative exponent and divider are not allowed in the same unit expression");
        }
        return new State(
                remainingInput,
                null,
                new ArrayList<>(this.parsedUnits),
                true,
                this.negativeExponentSeen,
                transitions);
    }

    public String remainingInputForNextState() {
        if (isAtEnd()) {
            return "";
        }
        return this.remainingInput.substring(1);
    }

    public boolean isAtEnd() {
        return this.remainingInput == null || this.remainingInput.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return dividerSeen == state.dividerSeen
                && Objects.equals(remainingInput, state.remainingInput)
                && Objects.equals(leftoverInput, state.leftoverInput)
                && Objects.equals(parsedUnits, state.parsedUnits)
                && Objects.equals(stateTransitionStack, state.stateTransitionStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                remainingInput, leftoverInput, parsedUnits, stateTransitionStack, dividerSeen);
    }

    public boolean isParseComplete() {
        return this.isAtEnd()
                && this.leftoverInput == null
                && this.stateTransitionStack.size()
                        == 1; // stack size 1 means we did not push a set of transition options that
        // we need to handle
    }
}
