/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.SyntaxErrorException;
import com.blazebit.persistence.parser.predicate.GtPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.predicate.PredicateQuantifier;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class QuantifierPredicateParsingTest extends AbstractParserTest {

    private Expression parseWithQuantifiedPredicates(String expr) {
        return ef().createSimpleExpression(expr, false, true, false, macroConfiguration, null);
    }

    private Expression parsePredicteWithQuantifiedPredicates(String expr) {
        return ef().createBooleanExpression(expr, true, macroConfiguration, null);
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
