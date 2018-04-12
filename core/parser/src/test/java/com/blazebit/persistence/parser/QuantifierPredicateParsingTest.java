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
        return ef().createSimpleExpression(expr, true, macroConfiguration, null);
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
