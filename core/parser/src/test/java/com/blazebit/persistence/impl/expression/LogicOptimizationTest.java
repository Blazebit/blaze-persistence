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

}
