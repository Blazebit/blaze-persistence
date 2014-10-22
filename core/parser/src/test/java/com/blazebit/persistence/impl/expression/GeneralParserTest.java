/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.impl.expression;

import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.BetweenPredicate;
import com.blazebit.persistence.impl.predicate.EqPredicate;
import com.blazebit.persistence.impl.predicate.GtPredicate;
import com.blazebit.persistence.impl.predicate.InPredicate;
import com.blazebit.persistence.impl.predicate.IsNullPredicate;
import com.blazebit.persistence.impl.predicate.LikePredicate;
import com.blazebit.persistence.impl.predicate.LtPredicate;
import com.blazebit.persistence.impl.predicate.MemberOfPredicate;
import com.blazebit.persistence.impl.predicate.OrPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class GeneralParserTest extends AbstractParserTest {

    @Test
    public void testSize() {
        Expression result = parse("SIZE(d.contacts)");
        assertEquals(function("SIZE", path("d", "contacts")), result);
        assertTrue(((PathExpression) ((FunctionExpression) result).getExpressions().get(0)).isUsedInCollectionFunction());
    }

    @Test
    public void testAggregateExpressionSinglePath() {
        Expression result = parse("AVG(age)");
        assertEquals(aggregate("AVG", path("age")), result);
    }

    @Test
    public void testAggregateExpressionMultiplePath() {
        Expression result = parse("AVG(d.age)");
        assertEquals(aggregate("AVG", path("d", "age")), result);
    }

    @Test
    public void testParser2() {
        Expression result = parse("d.problem.age");
        assertEquals(path("d", "problem", "age"), result);
    }

    @Test
    public void testParser3() {
        Expression result = parse("age");
        assertEquals(path("age"), result);
    }

    @Test
    public void testParserArithmetic1() {
        CompositeExpression result = (CompositeExpression) parseArithmeticExpr("d.age + SUM(d.children.age)");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 3);

        assertTrue(expressions.get(0).equals(path("d", "age")));
        assertTrue(expressions.get(1).equals(new FooExpression(" + ")));
        assertTrue(expressions.get(2).equals(aggregate("SUM", path("d", "children", "age"))));
    }

    @Test
    public void testParserArithmetic2() {
        CompositeExpression result = (CompositeExpression) parseArithmeticExpr("age + 1");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 2);

        assertTrue(expressions.get(0).equals(path("age")));
        assertTrue(expressions.get(1).equals(new FooExpression(" + 1")));
    }

    @Test
    public void testParserArithmetic3() {
        CompositeExpression result = (CompositeExpression) parseArithmeticExpr("age * 1");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 2);

        assertEquals(path("age"), expressions.get(0));
        assertEquals(new FooExpression(" * 1"), expressions.get(1));
    }

    @Test
    public void testParserArithmeticNoHiddenTokens() {
        CompositeExpression result = (CompositeExpression) parseArithmeticExpr("age+1");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 2);

        assertTrue(expressions.get(0).equals(path("age")));
        assertTrue(expressions.get(1).equals(new FooExpression("+1")));
    }

    @Test
    public void testNullLiteralExpression() {
        Expression result = parse("NULLIF(1,1)");
        assertEquals(function("NULLIF", new FooExpression("1"), new FooExpression("1")), result);
    }

    @Test
    public void testCountIdExpression() {
        Expression result = parse("COUNT(id)");
        assertEquals(aggregate("COUNT", path("id")), result);
    }

    @Test
    public void testKeyMapExpression() {
        Expression result = parse("KEY(map)");
        assertEquals(function("KEY", path("map")), result);
    }

    @Test
    public void testArrayExpression() {
        Expression result = parse("versions[test]");
        assertEquals(path("versions[test]"), result);
    }

    @Test
    public void testArrayIndexPath() {
        Expression result = parse("versions[test.x.y]");
        assertEquals(path("versions[test.x.y]"), result);
    }

    @Test
    public void testArrayIndexArithmetic() {
        Expression result = parse("versions[test.x.y + test.b]");

        PathExpression expected = new PathExpression();
        List<Expression> compositeExpressions = new ArrayList<Expression>();
        compositeExpressions.add(path("test", "x", "y"));
        compositeExpressions.add(new FooExpression(" + "));
        compositeExpressions.add(path("test", "b"));
        CompositeExpression expectedIndex = new CompositeExpression(compositeExpressions);
        expected.getExpressions().add(new ArrayExpression(new PropertyExpression("versions"), expectedIndex));

        assertEquals(expected, result);
    }

    @Test
    public void testArrayIndexArithmeticMixed() {
        Expression result = parse("versions[test.x.y + 1]");

        PathExpression expected = new PathExpression();
        List<Expression> compositeExpressions = new ArrayList<Expression>();
        compositeExpressions.add(path("test", "x", "y"));
        compositeExpressions.add(new FooExpression(" + 1"));
        CompositeExpression expectedIndex = new CompositeExpression(compositeExpressions);
        expected.getExpressions().add(new ArrayExpression(new PropertyExpression("versions"), expectedIndex));

        assertEquals(expected, result);
    }

    @Test
    public void testArrayIndexArithmeticLiteral() {
        Expression result = parse("versions[2 + 1]");
        PathExpression expected = new PathExpression();
        expected.getExpressions().add(new ArrayExpression(new PropertyExpression("versions"), new FooExpression("2 + 1")));

        assertEquals(expected, result);
    }

    @Test
    public void testMultipleArrayExpressions() {
        Expression result = parse("versions[test.x.y].owner[a.b.c]");
        assertEquals(path("versions[test.x.y]", "owner[a.b.c]"), result);
    }

    @Test
    public void testArrayInTheMiddle() {
        Expression result = parse("owner.versions[test.x.y].test");
        assertEquals(path("owner", "versions[test.x.y]", "test"), result);
    }

    @Test
    public void testArrayWithParameterIndex() {
        Expression result = parse("versions[:index]");
        assertEquals(path("versions[:index]"), result);
    }

    @Test(expected = SyntaxErrorException.class)
    public void testArrayWithInvalidParameterIndex() {
        parse("versions[:index.b]");
    }

    @Test
    public void testSingleElementArrayIndexPath1() {
        Expression result = parse("d[a]");
        assertEquals(path("d[a]"), result);
    }

    @Test
    public void testSingleElementArrayIndexPath2() {
        Expression result = parse("d.b[a]");
        assertEquals(path("d", "b[a]"), result);
    }

    @Test
    public void testSingleElementArrayIndexPath3() {
        Expression result = parse("d.b[a].c");
        assertEquals(path("d", "b[a]", "c"), result);
    }

    @Test
    public void testSingleElementArrayIndexPath4() {
        Expression result = parse("d.b[a].c[e]");
        assertEquals(path("d", "b[a]", "c[e]"), result);
    }

    @Test
    public void testSingleElementArrayIndexPath5() {
        Expression result = parse("d.b[a].c[e].f");
        assertEquals(path("d", "b[a]", "c[e]", "f"), result);
    }

    @Test
    public void testMultiElementArrayIndexPath1() {
        Expression result = parse("d[a.a]");
        assertEquals(path("d[a.a]"), result);
    }

    @Test
    public void testMultiElementArrayIndexPath2() {
        Expression result = parse("d.b[a.a]");
        assertEquals(path("d", "b[a.a]"), result);
    }

    @Test
    public void testMultiElementArrayIndexPath3() {
        Expression result = parse("d.b[a.a].c");
        assertEquals(path("d", "b[a.a]", "c"), result);
    }

    @Test
    public void testMultiElementArrayIndexPath4() {
        Expression result = parse("d.b[a.a].c[e.a]");
        assertEquals(path("d", "b[a.a]", "c[e.a]"), result);
    }

    @Test
    public void testMultiElementArrayIndexPath5() {
        Expression result = parse("d.b[a.a].c[e.a].f");
        assertEquals(path("d", "b[a.a]", "c[e.a]", "f"), result);
    }

    @Test
    public void testParameterArrayIndex1() {
        Expression result = parse("d[:a]");
        assertEquals(path("d[:a]"), result);
    }

    @Test
    public void testParameterArrayIndex2() {
        Expression result = parse("d.b[:a]");
        assertEquals(path("d", "b[:a]"), result);
    }

    @Test
    public void testParameterArrayIndex3() {
        Expression result = parse("d.b[:a].c");
        assertEquals(path("d", "b[:a]", "c"), result);
    }

    @Test
    public void testParameterArrayIndex4() {
        Expression result = parse("d.b[:a].c[:a]");
        assertEquals(path("d", "b[:a]", "c[:a]"), result);
    }

    @Test
    public void testParameterArrayIndex5() {
        Expression result = parse("d.b[:a].c[:a].f");
        assertEquals(path("d", "b[:a]", "c[:a]", "f"), result);
    }

    @Test
    public void testKeyFunctionArray() {
        Expression result = parse("KEY(localized[:locale])");
        assertEquals(function("KEY", path("localized[:locale]")), result);
    }

    @Test
    public void testKeyFunctionPath() {
        Expression result = parse("KEY(d.age)");
        assertEquals(function("KEY", path("d", "age")), result);
    }

    @Test
    public void testValueFunctionArray() {
        Expression result = parse("VALUE(localized[:locale])");
        assertEquals(function("VALUE", path("localized[:locale]")), result);
    }

    @Test
    public void testValueFunctionPath() {
        Expression result = parse("VALUE(d.age)");
        assertEquals(function("VALUE", path("d", "age")), result);
    }

    @Test
    public void testEntryFunctionArray() {
        Expression result = parse("ENTRY(localized[:locale])");
        assertEquals(function("ENTRY", path("localized[:locale]")), result);
    }

    @Test
    public void testEntryFunctionPath() {
        Expression result = parse("ENTRY(d.age)");
        assertEquals(function("ENTRY", path("d", "age")), result);
    }

    @Test(expected = SyntaxErrorException.class)
    public void testPathExpressionParsingNegative() {
        parsePath("ENTRY(d.age)");
    }

    @Test
    public void testPathExpressionParsingPositive() {
        PathExpression result = parsePath("d.age");
        assertEquals(path("d", "age"), result);
    }

    @Test
    public void testCaseWhenSwitchTrue() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN localized[:locale] NOT MEMBER OF supportedLocales THEN true ELSE false END");
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(
                new WhenClauseExpression(new MemberOfPredicate(path("localized[:locale]"), path("supportedLocales"), true), foo("true"))),
                foo("false"));
        assertEquals(expected, result);
    }

    @Test
    public void testCaseWhenMultipleWhenClauses() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x = 2 THEN true WHEN a.x = 3 THEN false ELSE false END");
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(
                new WhenClauseExpression(new EqPredicate(path("a", "x"), foo("2")), foo("true")),
                new WhenClauseExpression(new EqPredicate(path("a", "x"), foo("3")), foo("false"))
        ), foo("false"));

        assertEquals(expected, result);
    }

    @Test
    public void testCaseWhenSize() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN SIZE(d.contacts) > 2 THEN 2 ELSE SIZE(d.contacts) END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new GtPredicate(function("SIZE", path("d", "contacts")), foo("2")), foo("2"))), function("SIZE", path("d", "contacts")));
        assertEquals(expected, result);
    }

    @Test
    public void testComplexCaseWhen() {
        FunctionExpression result = (FunctionExpression) parse(""
                + "CONCAT(\n"
                + "	COALESCE(\n"
                + "		CONCAT(\n"
                + "			NULLIF(\n"
                + "				CONCAT(\n"
                + "					CASE WHEN LENGTH(COALESCE(zip, '')) > 0 OR LENGTH(COALESCE(city, '')) > 0 THEN COALESCE(CONCAT(NULLIF(street, ''), ', '), '') ELSE COALESCE(street, '') END,\n"
                + "					CASE WHEN LENGTH(COALESCE(city, '')) > 0 THEN COALESCE(CONCAT(NULLIF(zip, ''), ' '), '') ELSE COALESCE(zip, '') END,\n"
                + "					COALESCE(city, '')\n"
                + "				),\n"
                + "				''\n"
                + "			),\n"
                + "			' - '\n"
                + "		),\n"
                + "		''\n"
                + "	),\n"
                + "	'test'"
                + ")");

        FunctionExpression expected
                = function("CONCAT",
                        function("COALESCE",
                                function("CONCAT",
                                        function("NULLIF",
                                                function("CONCAT",
                                                        new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new OrPredicate(
                                                                                        new GtPredicate(function("LENGTH", function("COALESCE", path("zip"), foo("''"))), foo("0")),
                                                                                        new GtPredicate(function("LENGTH", function("COALESCE", path("city"), foo("''"))), foo("0"))
                                                                                ), function("COALESCE", function("CONCAT", function("NULLIF", path("street"), foo("''")), foo("', '")), foo("''")))),
                                                                function("COALESCE", path("street"), foo("''"))),
                                                        new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(
                                                                                new GtPredicate(function("LENGTH", function("COALESCE", path("city"), foo("''"))), foo("0")),
                                                                                function("COALESCE", function("CONCAT", function("NULLIF", path("zip"), foo("''")), foo("' '")), foo("''")))),
                                                                function("COALESCE", path("zip"), foo("''"))),
                                                        function("COALESCE", path("city"), foo("''"))
                                                ),
                                                foo("''")
                                        ),
                                        foo("' - '")
                                ),
                                foo("''")
                        ),
                        foo("'test'")
                );
        assertEquals(expected, result);
    }

    @Test
    public void testMemberOf() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN x.a MEMBER OF y.a THEN 0 ELSE 2 END");

        Predicate condition = new MemberOfPredicate(path("x", "a"), path("y", "a"));
        GeneralCaseExpression expectedExpr = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(condition, new FooExpression("0"))), new FooExpression("2"));
        assertEquals(expectedExpr, result);
    }

    @Test(expected = SyntaxErrorException.class)
    public void testMemberOfInvalidUse() {
        parse("x.a MEMBER OF y.a");
    }

    @Test
    public void testTypeFunctionPath() {
        Expression result = parse("TYPE(d.age)");
        assertEquals(function("TYPE", path("d", "age")), result);
    }

    @Test
    public void testTypeFunctionParameter() {
        Expression result = parse("TYPE(:test)");
        assertEquals(function("TYPE", parameter("test")), result);
    }

    @Test
    public void testTypeFunctionSingleElementPath() {
        Expression result = parse("TYPE(age)");
        assertEquals(function("TYPE", path("age")), result);
    }

    @Test
    public void testFunctionInvocation() {
        Expression result = parse("FUNCTION('myfunc', a.b, 'b', 12)");
        assertEquals(function("FUNCTION", foo("'myfunc'"), path("a", "b"), new FooExpression("'b'"), new FooExpression("12")), result);
    }

    @Test
    public void testLength() {
        Expression result = parse("LENGTH('myfunc')");
        assertEquals(function("LENGTH", new FooExpression("'myfunc'")), result);
    }

    @Test
    public void testOuter() {
        Expression result = parseSubqueryExpression("OUTER(a.b.c)");
        assertEquals(function("OUTER", path("a", "b", "c")), result);
    }

    @Test
    public void testCompositeOuter() {
        Expression result = parseSubqueryExpression("OUTER(a.b.c) + OUTER(z.x)");
        assertEquals(compose(function("OUTER", path("a", "b", "c")), foo(" + "), function("OUTER", path("z", "x"))), result);
    }

    @Test(expected = SyntaxErrorException.class)
    public void testInvalidOuter() {
        parse("OUTER(a.b.c)");
    }

    @Test
    public void testCoalesce() {
        Expression result = parseSubqueryExpression("COALESCE(a.b.c, a.b, a.a, 'da', 1)");
        assertEquals(function("COALESCE", path("a", "b", "c"), path("a", "b"), path("a", "a"), new FooExpression("'da'"), new FooExpression("1")), result);
    }

    @Test
    public void testInParameter() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x IN (:abc) THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new InPredicate(path("a", "x"), new ParameterExpression("abc")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testInParameterNoParanthesis() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x IN :abc THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new InPredicate(path("a", "x"), new ParameterExpression("abc")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testInNumericLiterals() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x IN (1, 2, 3, 4) THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new InPredicate(path("a", "x"), foo("(1,2,3,4)")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testInCharacterLiterals() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x IN ('1', '2', '3', '4') THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new InPredicate(path("a", "x"), foo("('1','2','3','4')")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testIsNull() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x IS NULL THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new IsNullPredicate(path("a", "x")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testIsNotNull() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x IS NOT NULL THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new IsNullPredicate(path("a", "x"), true), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testLike() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x LIKE 'abc' THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new LikePredicate(path("a", "x"), parseStringExpr("'abc'"), true, null), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testLikeEscapeParameter() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x LIKE 'abc' ESCAPE :x THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new LikePredicate(path("a", "x"), parseStringExpr("'abc'"), true, 'x'), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testLikeEscapeLiteral() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x LIKE 'abc' ESCAPE 'x' THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new LikePredicate(path("a", "x"), parseStringExpr("'abc'"), true, 'x'), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testNotLike() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x NOT LIKE 'abc' THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new LikePredicate(path("a", "x"), parseStringExpr("'abc'"), true, null, true), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testBetweenArithmetic() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x BETWEEN 1 AND 2 THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new BetweenPredicate(path("a", "x"), foo("1"), foo("2")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testNotBetweenArithmetic() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x NOT BETWEEN 1 AND 2 THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new BetweenPredicate(path("a", "x"), foo("1"), foo("2"), true), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testBetweenString() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x BETWEEN 'ab' AND 'zb' THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new BetweenPredicate(path("a", "x"), foo("'ab'"), foo("'zb'")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testNotBetweenString() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x NOT BETWEEN 'ab' AND 'zb' THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new BetweenPredicate(path("a", "x"), foo("'ab'"), foo("'zb'"), true), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testBetweenCharacter() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x BETWEEN 'a' AND 'z' THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new BetweenPredicate(path("a", "x"), foo("'a'"), foo("'z'")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testNotBetweenCharacter() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x NOT BETWEEN 'a' AND 'z' THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new BetweenPredicate(path("a", "x"), foo("'a'"), foo("'z'"), true), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testBetweenDate() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x BETWEEN (d '1991-05-21') AND (d '1991-05-22') THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new BetweenPredicate(path("a", "x"), foo("(d '1991-05-21')"), foo("(d '1991-05-22')")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testNotBetweenDate() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x NOT BETWEEN (d '1991-05-21') AND (d '1991-05-22') THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new BetweenPredicate(path("a", "x"), foo("(d '1991-05-21')"), foo("(d '1991-05-22')"), true), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test(expected = SyntaxErrorException.class)
    public void testInvalidOrderBy1() {
        parseOrderBy("a.b + b.c");
    }

    @Test(expected = SyntaxErrorException.class)
    public void testInvalidOrderBy2() {
        parseOrderBy("SIZE(a.b)");
    }

    @Test
    public void testOrderBy() {
        PathExpression pathExpr = (PathExpression) parseOrderBy("id");
        assertEquals(path("id"), pathExpr);
    }

    @Test
    public void testCaseWhenAndOr() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN x.a = y.a OR c.a < 9 AND b - c = 2 THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new OrPredicate(new EqPredicate(path("x", "a"), path("y", "a")), new AndPredicate(new LtPredicate(path("c", "a"), foo("9")), new EqPredicate(compose(path("b"), foo(" - "), path("c")), foo("2")))), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testNot1() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN NOT(x.a = y.a) OR c.a < 9 AND b - c = 2 THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new OrPredicate(new EqPredicate(path("x", "a"), path("y", "a"), true), new AndPredicate(new LtPredicate(path("c", "a"), foo("9")), new EqPredicate(compose(path("b"), foo(" - "), path("c")), foo("2")))), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testNot2() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN NOT(localized[:locale].name = localized[:locale].description) THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new EqPredicate(path("localized[:locale]", "name"), path("localized[:locale]", "description"), true), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testNot3() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN NOT(x.a = y.a OR c.a < 9 AND b - c = 2) THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(not(new OrPredicate(new EqPredicate(path("x", "a"), path("y", "a")), new AndPredicate(new LtPredicate(path("c", "a"), foo("9")), new EqPredicate(compose(path("b"), foo(" - "), path("c")), foo("2"))))), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
}
