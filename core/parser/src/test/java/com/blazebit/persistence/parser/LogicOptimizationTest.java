/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.parser;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.GeneralCaseExpression;
import com.blazebit.persistence.parser.expression.WhenClauseExpression;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.parser.predicate.GtPredicate;
import com.blazebit.persistence.parser.predicate.LtPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
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
