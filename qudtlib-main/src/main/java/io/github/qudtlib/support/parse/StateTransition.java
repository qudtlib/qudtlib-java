package io.github.qudtlib.support.parse;

import java.util.List;
import java.util.function.Function;

interface StateTransition extends Function<State, List<State>> {

    boolean mayMatchLater(String string);

    DividerTransition DIVIDER = new DividerTransition();
    UnitTransition UNIT = new UnitTransition();
    ExponentTransition EXPONENT = new ExponentTransition();
    DotTransition DOT = new DotTransition();
    WhitespaceTransition WHITESPACE = new WhitespaceTransition();
    OpeningBracketTransition OPENING_BRACKET = new OpeningBracketTransition();
    ClosingBracketTransition CLOSING_BRACKET = new ClosingBracketTransition();
    OneTransition ONE = new OneTransition();
}
