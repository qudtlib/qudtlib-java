package io.github.qudtlib.support.parse;

import static java.util.function.Predicate.not;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.exception.IncompleteDataException;
import io.github.qudtlib.model.DerivedUnitSearchMode;
import io.github.qudtlib.model.FactorUnits;
import io.github.qudtlib.model.QuantityKind;
import io.github.qudtlib.model.Unit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UnitParser {

    String input;
    QuantityKind quantityKind;

    public UnitParser(String input) {
        this(input, null);
    }

    public UnitParser(String input, QuantityKind quantityKind) {
        this.input = input;
        this.quantityKind = quantityKind;
    }

    public Set<Unit> parse() {
        Set<State> oldStates = new HashSet<>();
        SortedSet<State> states =
                new TreeSet<>(
                        Comparator.comparing(State::badness)
                                .thenComparing(s -> -s.getParsedUnits().size())
                                .thenComparing(
                                        s ->
                                                -(s.getRemainingInput().length()
                                                        + Optional.ofNullable(s.getLeftoverInput())
                                                                .map(String::length)
                                                                .orElse(0)))
                                .thenComparing(Object::hashCode));
        State initialState =
                new State(
                        this.input,
                        StateTransition.UNIT,
                        StateTransition.WHITESPACE,
                        StateTransition.DIVIDER,
                        StateTransition.ONE);
        states.add(initialState);
        List<State> finishedStates = new ArrayList<>();
        boolean finished = false;
        int step = 0;
        while (!states.isEmpty()) {
            /*
                        System.out.println("\nstep " + (step++));
                        System.out.println();
                        System.out.println("states:");
                        states.stream().forEach(System.out::println);
                        System.out.println();
            */

            Iterator<State> it = states.iterator();
            State currentState = it.next();
            it.remove();
            List<State> next = currentState.nextTransition();
            next.stream().filter(State::isParseComplete).forEach(finishedStates::add);
            next.stream().filter(not(State::isParseComplete)).forEach(states::add);
            if (!finishedStates.isEmpty()) {
                Set<Unit> results = new HashSet<>();
                for (State finishedState : finishedStates) {
                    // System.out.println("finished state: " + finishedState);
                    List<ParsedUnit> parsedUnits = finishedState.getParsedUnits();
                    if (!parsedUnits.isEmpty()) {
                        FactorUnits factorUnits =
                                new FactorUnits(
                                        parsedUnits.stream()
                                                .map(ParsedUnit::getFactorUnit)
                                                .collect(Collectors.toUnmodifiableList()));
                        if (this.quantityKind != null
                                && !this.quantityKind.isDeprecated()
                                && !this.quantityKind.equals(Qudt.QuantityKinds.Unknown)
                                && this.quantityKind.getDimensionVector().isPresent()) {
                            try {
                                if (this.quantityKind
                                        .getDimensionVector()
                                        .get()
                                        .equals(factorUnits.getDimensionVector())) {
                                    findUnits(factorUnits, parsedUnits).forEach(results::add);
                                }
                            } catch (IncompleteDataException e) {
                                // ignore: unit will not be found
                            }
                        } else {
                            // cannot filter by dim vector, add everyting
                            findUnits(factorUnits, parsedUnits).forEach(results::add);
                        }
                    }
                }
                //                System.out.println("results: " + results);
                if (!results.isEmpty()) {
                    if (this.quantityKind != null) {
                        Set<Unit> intermediateResults = new HashSet<>(results);
                        intermediateResults.retainAll(this.quantityKind.getApplicableUnits());
                        //                        System.out.println("intermediateResults: " +
                        // intermediateResults);
                        if (intermediateResults.isEmpty()) {
                            intermediateResults =
                                    results.stream()
                                            .filter(
                                                    u ->
                                                            u.getDimensionVector()
                                                                    .map(
                                                                            dv ->
                                                                                    dv.equals(
                                                                                            this
                                                                                                    .quantityKind
                                                                                                    .getDimensionVector()
                                                                                                    .orElse(
                                                                                                            null)))
                                                                    .orElse(true))
                                            .collect(Collectors.toSet());
                        }
                        results = intermediateResults;
                    }
                    if (results.size() > 1) {
                        results = this.findBetterMatches(results);
                    }
                    if (results.size() > 1) {
                        results = this.retainOnlyExactMatchesIfPresent(results);
                    }
                    if (!results.isEmpty()) {
                        return results;
                    }
                }
                finishedStates.clear();
            }
        }
        return Set.of();
    }

    private Set<Unit> retainOnlyExactMatchesIfPresent(Set<Unit> results) {
        Set<Unit> exactMatches =
                results.stream()
                        .filter(
                                u ->
                                        this.input.equals(u.getSymbol().orElse("[no symbol]"))
                                                || this.input.equals(
                                                        u.getUcumCode().orElse("[no ucumCode]")))
                        .collect(Collectors.toSet());
        if (!exactMatches.isEmpty()) {
            return exactMatches;
        }
        return results;
    }

    private Stream<Unit> findUnits(FactorUnits factorUnits, List<ParsedUnit> parsedUnits) {
        List<Unit> matchingUnits =
                Qudt.unitsFromFactorUnits(DerivedUnitSearchMode.ALL, factorUnits.getFactorUnits());
        //        System.out.println("matchingUnits: " + matchingUnits);
        return matchingUnits.stream().filter(u -> containsAllParsedUnits(u, parsedUnits));
    }

    private boolean containsAllParsedUnits(Unit u, List<ParsedUnit> parsedUnits) {
        FactorUnits required =
                new FactorUnits(parsedUnits.stream().map(ParsedUnit::getFactorUnit).toList());
        FactorUnits toCheck = u.getFactorUnits();
        boolean result = FactorUnits.ofUnit(u).equals(required) || toCheck.equals(required);
        if (!result) {
            //            System.out.println(
            //                    String.format(
            //                            "unit %s has factorUnits %s, which are not the same as
            // %s",
            //                            u.getIriLocalname(), toCheck.toString(),
            // required.toString()));
        }
        return result;
    }

    private Set<Unit> findBetterMatches(Set<Unit> results) {
        Set<Unit> better =
                results.stream()
                        .filter(u -> UnitTransition.isRelaxedMatchForUnit(input, u))
                        .collect(Collectors.toSet());
        if (better.size() > 1 && this.quantityKind != null) {
            Set<Unit> onlyWithExactQuantitykind =
                    results.stream()
                            .filter(u -> u.getQuantityKinds().contains(this.quantityKind))
                            .collect(Collectors.toSet());
            if (!onlyWithExactQuantitykind.isEmpty()) {
                return onlyWithExactQuantitykind;
            }
        }
        if (!better.isEmpty()) {
            return better;
        }
        return results;
    }

    @Override
    public String toString() {
        return "UnitParser{"
                + "input='"
                + input
                + '\''
                + ", quantityKind="
                + Qudt.NAMESPACES.quantityKind.abbreviate(quantityKind.getIri())
                + '}';
    }
}
