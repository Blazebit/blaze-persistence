package com.blazebit.persistence.impl.expression;

import com.blazebit.persistence.impl.predicate.*;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 05.08.2016.
 */
public class LogicOptimizationTest extends AbstractParserTest {

    @Test
    public void testMaintainNegationStructure() {
        Predicate result = parsePredicate("NOT(NOT(a > b))", false);

        Predicate expected = wrapNot(not(new GtPredicate(path("a"), path("b"))));
        assertEquals(expected, result);
    }

    @Test
    public void testOptimizeNegationStructure1() {
        Predicate result = parsePredicateOptimized("NOT(NOT(a > b))", false);
        assertEquals(new GtPredicate(path("a"), path("b")), result);
    }

    @Test
    public void testOptimizeNegationStructure2() {
        Predicate result = parsePredicateOptimized("NOT(NOT(a > b) OR a < x)", false);
        Predicate expected = new CompoundPredicate(CompoundPredicate.BooleanOperator.AND,
                new GtPredicate(path("a"), path("b")),
                new LtPredicate(path("a"), path("x"), true));
        assertEquals(expected, result);
    }

    @Test
    public void testOptimizeNegationStructure3() {
        Predicate result = parsePredicateOptimized("NOT(NOT(a > b) AND a < x)", false);
        Predicate expected = new CompoundPredicate(CompoundPredicate.BooleanOperator.OR,
                new GtPredicate(path("a"), path("b")),
                new LtPredicate(path("a"), path("x"), true));
        assertEquals(expected, result);
    }

    @Test
    public void testOptimizeNegationStructure4() {
        Expression result = parseOptimized("CASE WHEN NOT(NOT(a > b)) THEN 1 ELSE 0 END");
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(
                new WhenClauseExpression(new GtPredicate(path("a"), path("b")), _int("1"))
        ), _int("0"));
        assertEquals(expected, result);
    }

}
