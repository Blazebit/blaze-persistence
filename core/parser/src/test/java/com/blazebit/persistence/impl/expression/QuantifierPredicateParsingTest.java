package com.blazebit.persistence.impl.expression;

import com.blazebit.persistence.impl.predicate.GtPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateQuantifier;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 10.08.2016.
 */
public class QuantifierPredicateParsingTest extends AbstractParserTest {

    private Expression parseWithQuantifiedPredicates(String expr) {
        return ef.createSimpleExpression(expr, true);
    }

    private Expression parsePredicteWithQuantifiedPredicates(String expr) {
        return ef.createBooleanExpression(expr, true);
    }

    @Test
    public void testAny() {
        Predicate result1 = parsePredicate("x > ANY(a)", true);
        Predicate result2 = parsePredicate("x > ANY a", true);
        Predicate expected = new GtPredicate(path("x"), path("a"), PredicateQuantifier.ANY, false);
        assertEquals(expected, result1);
        assertEquals(expected, result2);
    }

    @Test
    public void testAll() {
        Predicate result1 = parsePredicate("x > ALL(a)", true);
        Predicate result2 = parsePredicate("x > ALL a", true);
        Predicate expected = new GtPredicate(path("x"), path("a"), PredicateQuantifier.ALL, false);
        assertEquals(expected, result1);
        assertEquals(expected, result2);
    }

    @Test
    public void testSome() {
        Predicate result1 = parsePredicate("x > SOME(a)", true);
        Predicate result2 = parsePredicate("x > SOME a", true);
        Predicate expected = new GtPredicate(path("x"), path("a"), PredicateQuantifier.ANY, false);
        assertEquals(expected, result1);
        assertEquals(expected, result2);
    }

    @Test(expected = SyntaxErrorException.class)
    public void testNotAllowed1() {
        parsePredicate("x > ALL(a)", false);
    }

    @Test(expected = SyntaxErrorException.class)
    public void testNotAllowed2() {
        parse("CASE WHEN x > ALL(a) THEN 0 ELSE 1 END");
    }

    @Test
    public void testAllowed1() {
        parsePredicate("x > ALL(a)", true);
    }

    @Test
    public void testAllowed2() {
        parseWithQuantifiedPredicates("CASE WHEN x > ALL(a) THEN 0 ELSE 1 END");
    }
}
