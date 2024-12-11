package io.github.qudtlib.support.parse;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.FactorUnit;
import io.github.qudtlib.model.Unit;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class UnitTransition extends AbstractStateTransition {
    private static final Pattern P_UNIT = Pattern.compile(".*");

    public UnitTransition() {
        super("Unit", P_UNIT);
    }

    @Override
    public boolean mayMatchLater(String string) {
        return isRelaxedMatchStartForUnit(string);
    }

    @Override
    protected List<State> parse(
            State state,
            String matchedToken,
            List<StateTransition> nextTransitions,
            List<StateTransition> requiredClosingTransitions) {
        Set<Unit> units = getMatchingUnits(matchedToken);
        int exponent = state.isDividerSeen() ? -1 : 1;
        List<ParsedUnit> parsedUnitsBase = state.getParsedUnits();
        List<List<ParsedUnit>> variants =
                units.stream()
                        .map(
                                u -> {
                                    List<ParsedUnit> variant = new ArrayList<>(parsedUnitsBase);

                                    variant.add(
                                            new ParsedUnit(
                                                    makeFactorUnit(u, exponent), matchedToken));
                                    return variant;
                                })
                        .collect(Collectors.toList());
        Deque<List<StateTransition>> transitionStack =
                new ArrayDeque<>(state.getStateTransitionStack());
        if (!state.isAtEnd() && !state.isDividerSeen() && !state.isNegativeExponentSeen()) {
            nextTransitions.add(StateTransition.DIVIDER);
        }
        pushTransitions(transitionStack, requiredClosingTransitions);
        pushTransitions(transitionStack, nextTransitions);
        if (variants.isEmpty()) {
            return List.of();
        }
        return variants.stream()
                .map(
                        variant ->
                                new State(
                                        state.remainingInputForNextState(),
                                        null,
                                        variant,
                                        state.isDividerSeen(),
                                        state.isNegativeExponentSeen(),
                                        transitionStack))
                .toList();
    }

    private FactorUnit makeFactorUnit(Unit u, int exponent) {
        List<FactorUnit> factorUnitList = u.getFactorUnits().getFactorUnits();
        /*if (factorUnitList.size() == 1 && factorUnitList.get(0).getExponent() != 1) {
            FactorUnit fu = factorUnitList.get(0);
            return new FactorUnit(fu.getUnit(), fu.getExponent() * exponent);
        }*/
        return new FactorUnit(u, exponent);
    }

    static Set<Unit> getMatchingUnits(String effectiveToken) {
        return Stream.of(
                        Qudt.unitsBySymbol(effectiveToken, false, false).stream(),
                        Qudt.unitsByIriLocalname(effectiveToken, false, true).stream(),
                        Qudt.unitsByLabel(effectiveToken, false, true).stream(),
                        Qudt.unitsByUcumCode(effectiveToken, false, false).stream())
                .flatMap(Function.identity())
                .collect(Collectors.toSet());
    }

    static boolean isRelaxedMatchStartForUnit(String effectiveToken) {
        effectiveToken = effectiveToken.replaceFirst("^1/", "/");
        return !(Qudt.unitsBySymbol(effectiveToken, true, false).isEmpty()
                && Qudt.unitsByIriLocalname(effectiveToken, true, true).isEmpty()
                && Qudt.unitsByLabel(effectiveToken, true, true).isEmpty()
                && Qudt.unitsByUcumCode(effectiveToken, true, false).isEmpty());
    }

    static boolean isRelaxedMatchForUnit(String effectiveToken, Unit u) {
        final String cmp = effectiveToken.replaceFirst("^1/", "/");
        return cmp.equals(u.getSymbol().orElse(null))
                || cmp.equalsIgnoreCase(u.getUcumCode().orElse(null))
                || cmp.equalsIgnoreCase(u.getIriLocalname())
                || u.getLabels().stream().anyMatch(label -> label.getString().equalsIgnoreCase(cmp))
                || cmp.equals(u.getUcumCode().orElse(null));
    }

    @Override
    protected List<StateTransition> getRequiredClosingTransitions() {
        return List.of();
    }

    @Override
    protected List<StateTransition> getAllowedNextTransitions() {
        return List.of(
                StateTransition.UNIT,
                StateTransition.EXPONENT,
                StateTransition.DOT,
                StateTransition.WHITESPACE,
                StateTransition.OPENING_BRACKET);
    }
}
