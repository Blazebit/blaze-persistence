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

import com.blazebit.persistence.impl.predicate.*;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class GeneralParserTest extends AbstractParserTest {

    @Test
    public void testSoftKeywordsMultipleKeywordsAsSimpleUpperPath() {
        Expression result = parse("ANDOR");
        assertEquals(path("ANDOR"), result);
    }
    
    @Test
    public void testSoftKeywordsMultipleKeywordsAsSimplePath() {
        Expression result = parse("andOr");
        assertEquals(path("andOr"), result);
    }
    @Test
    public void testSoftKeywordsMultipleKeywordsAsUpperPath() {
        Expression result = parse("entity.ANDOR");
        assertEquals(path("entity", "ANDOR"), result);
    }
    
    @Test
    public void testSoftKeywordsMultipleKeywordsAsPath() {
        Expression result = parse("entity.andOr");
        assertEquals(path("entity", "andOr"), result);
    }
    
    @Test
    public void testSoftKeywordsTypeAsUpperPath() {
        Expression result = parse("entity.TYPE");
        assertEquals(path("entity", "TYPE"), result);
    }
    
    @Test
    public void testSoftKeywordsTypeAsPath() {
        Expression result = parse("entity.type");
        assertEquals(path("entity", "type"), result);
    }
    
    @Test
    public void testSoftKeywordsTypeAsSimpleUpperPath() {
        Expression result = parse("TYPE");
        assertEquals(path("TYPE"), result);
    }
    
    @Test
    public void testSoftKeywordsTypeAsSimplePath() {
        Expression result = parse("type");
        assertEquals(path("type"), result);
    }

    @Test
    public void testSoftKeywordsTypeAsUpperFunction() {
        Expression result = parse("TYPE(entity.type)");
        assertEquals(typeFunction(path("entity", "type")), result);
    }

    @Test
    public void testSoftKeywordsTypeAsFunction() {
        Expression result = parse("type(entity.type)");
        assertEquals(typeFunction(path("entity", "type")), result);
    }
    
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
        ArithmeticExpression result = (ArithmeticExpression) parseArithmeticExpr("d.age + SUM(d.children.age)");

        assertEquals(path("d", "age"), result.getLeft());
        assertEquals(ArithmeticOperator.ADDITION, result.getOp());
        assertEquals(aggregate("SUM", path("d", "children", "age")), result.getRight());
    }

    @Test
    public void testParserArithmetic2() {
        ArithmeticExpression result = (ArithmeticExpression) parseArithmeticExpr("age + 1");

        assertEquals(path("age"), result.getLeft());
        assertEquals(ArithmeticOperator.ADDITION, result.getOp());
        assertEquals(_int("1"), result.getRight());
    }

    @Test
    public void testParserArithmetic3() {
        ArithmeticExpression result = (ArithmeticExpression) parseArithmeticExpr("age * 1");

        assertEquals(path("age"), result.getLeft());
        assertEquals(ArithmeticOperator.MULTIPLICATION, result.getOp());
        assertEquals(_int("1"), result.getRight());
    }

    @Test
    public void testParserArithmeticNoHiddenTokens() {
        ArithmeticExpression result = (ArithmeticExpression) parseArithmeticExpr("age+1");

        assertEquals(path("age"), result.getLeft());
        assertEquals(ArithmeticOperator.ADDITION, result.getOp());
        assertEquals(_int("1"), result.getRight());
    }

    @Test
    public void testNullLiteralExpression() {
        Expression result = parse("NULLIF(1,1)");
        assertEquals(function("NULLIF", _int("1"), _int("1")), result);
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
    public void testArrayStringLiteralIndex() {
        Expression result = parse("versions['test']");
        assertEquals(path("versions['test']"), result);
    }

    @Test
    public void testArrayIndexPath() {
        Expression result = parse("versions[test.x.y]");
        assertEquals(path("versions[test.x.y]"), result);
    }

    @Ignore("#210")
    @Test
    public void testArrayIndexArithmetic() {
        Expression result = parse("versions[test.x.y + test.b]");

        PathExpression expected = new PathExpression();
        ArithmeticExpression expectedIndex = add(path("test", "x", "y"), path("test", "b"));
        expected.getExpressions().add(new ArrayExpression(new PropertyExpression("versions"), expectedIndex));

        assertEquals(expected, result);
    }

    @Ignore("#210")
    @Test
    public void testArrayIndexArithmeticMixed() {
        Expression result = parse("versions[test.x.y + 1]");

        PathExpression expected = new PathExpression();
        ArithmeticExpression expectedIndex = add(path("test", "x", "y"), _int("1"));
        expected.getExpressions().add(new ArrayExpression(new PropertyExpression("versions"), expectedIndex));

        assertEquals(expected, result);
    }

    @Ignore("#210")
    @Test
    public void testArrayIndexArithmeticLiteral() {
        Expression result = parse("versions[2 + 1]");
        PathExpression expected = new PathExpression();
        expected.getExpressions().add(new ArrayExpression(new PropertyExpression("versions"), add(_int("2"), _int("1"))));

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
                new WhenClauseExpression(new MemberOfPredicate(path("localized[:locale]"), path("supportedLocales"), true), _boolean(true))),
                _boolean(false));
        assertEquals(expected, result);
    }
    
    @Test
    public void testSimpleCaseWhen() {
    	SimpleCaseExpression result = (SimpleCaseExpression) parse("CASE a.b WHEN 1 THEN true ELSE false END");
    	SimpleCaseExpression expected = new SimpleCaseExpression(path("a", "b"), Arrays.asList(
                new WhenClauseExpression(_int("1"), _boolean(true))),
                _boolean(false));
        assertEquals(expected, result);
    }

    @Test
    public void testCaseWhenMultipleWhenClauses() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x = 2 THEN true WHEN a.x = 3 THEN false ELSE false END");
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(
                new WhenClauseExpression(new EqPredicate(path("a", "x"), _int("2")), _boolean(true)),
                new WhenClauseExpression(new EqPredicate(path("a", "x"), _int("3")), _boolean(false))
        ), _boolean(false));

        assertEquals(expected, result);
    }

    @Test
    public void testCaseWhenSize() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN SIZE(d.contacts) > 2 THEN 2 ELSE SIZE(d.contacts) END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new GtPredicate(function("SIZE", path("d", "contacts")), _int("2")), _int("2"))), function("SIZE", path("d", "contacts")));
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
                                                        new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new CompoundPredicate(CompoundPredicate.BooleanOperator.OR,
                                                                                        new GtPredicate(function("LENGTH", function("COALESCE", path("zip"), _string(""))), _int("0")),
                                                                                        new GtPredicate(function("LENGTH", function("COALESCE", path("city"), _string(""))), _int("0"))
                                                                                ), function("COALESCE", function("CONCAT", function("NULLIF", path("street"), _string("")), _string(", ")), _string("")))),
                                                                function("COALESCE", path("street"), _string(""))),
                                                        new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(
                                                                                new GtPredicate(function("LENGTH", function("COALESCE", path("city"), _string(""))), _int("0")),
                                                                                function("COALESCE", function("CONCAT", function("NULLIF", path("zip"), _string("")), _string(" ")), _string("")))),
                                                                function("COALESCE", path("zip"), _string(""))),
                                                        function("COALESCE", path("city"), _string(""))
                                                ),
                                                _string("")
                                        ),
                                        _string(" - ")
                                ),
                                _string("")
                        ),
                        _string("test")
                );
        assertEquals(expected, result);
    }

    @Test
    public void testMemberOf() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN x.a MEMBER OF y.a THEN 0 ELSE 2 END");

        Predicate condition = new MemberOfPredicate(path("x", "a"), path("y", "a"));
        GeneralCaseExpression expectedExpr = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(condition, _int("0"))), _int("2"));
        assertEquals(expectedExpr, result);
    }

    @Test(expected = SyntaxErrorException.class)
    public void testMemberOfInvalidUse() {
        parse("x.a MEMBER OF y.a");
    }

    @Test
    public void testTypeFunctionPath() {
        Expression result = parse("TYPE(d.age)");
        assertEquals(typeFunction(path("d", "age")), result);
    }

    @Test
    public void testTypeFunctionParameter() {
        Expression result = parse("TYPE(:test)");
        assertEquals(typeFunction(parameter("test")), result);
    }

    @Test
    public void testTypeFunctionSingleElementPath() {
        Expression result = parse("TYPE(age)");
        assertEquals(typeFunction(path("age")), result);
    }

    @Test
    public void testFunctionInvocation() {
        Expression result = parse("FUNCTION('myfunc', a.b, 'b', 12)");
        assertEquals(function("FUNCTION", _string("myfunc"), path("a", "b"), _string("b"), _int("12")), result);
    }

    @Test
    public void testLength() {
        Expression result = parse("LENGTH('myfunc')");
        assertEquals(function("LENGTH", _string("myfunc")), result);
    }

    @Test
    public void testOuter() {
        Expression result = parseSubqueryExpression("OUTER(a.b.c)");
        assertEquals(function("OUTER", path("a", "b", "c")), result);
    }

    @Test
    public void testCompositeOuter() {
        Expression result = parseSubqueryExpression("OUTER(a.b.c) + OUTER(z.x)");
        assertEquals(add(function("OUTER", path("a", "b", "c")), function("OUTER", path("z", "x"))), result);
    }

    @Test(expected = SyntaxErrorException.class)
    public void testInvalidOuter() {
        parse("OUTER(a.b.c)");
    }

    @Test
    public void testCoalesce() {
        Expression result = parseSubqueryExpression("COALESCE(a.b.c, a.b, a.a, 'da', 1)");
        assertEquals(function("COALESCE", path("a", "b", "c"), path("a", "b"), path("a", "a"), _string("da"), _int("1")), result);
    }

    @Test
    public void testInParameter() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x IN (:abc) THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new InPredicate(path("a", "x"), new ParameterExpression("abc")), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testInMultipleParameterAndLiteral() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x IN (:abc, :def, 3) THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new InPredicate(path("a", "x"), new ParameterExpression("abc"), new ParameterExpression("def"), _int("3")), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testInParameterNoParanthesis() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x IN :abc THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new InPredicate(path("a", "x"), new ParameterExpression("abc", null, true)), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testInNumericLiterals() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x IN (1, 2, 3, 4) THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new InPredicate(path("a", "x"), _int("1"), _int("2"), _int("3"), _int("4")), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testInCharacterLiterals() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x IN ('1', '2', '3', '4') THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new InPredicate(path("a", "x"), _string("1"), _string("2"), _string("3"), _string("4")), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testIsNull() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x IS NULL THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new IsNullPredicate(path("a", "x")), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testIsNotNull() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x IS NOT NULL THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new IsNullPredicate(path("a", "x"), true), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testLike() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x LIKE 'abc' THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new LikePredicate(path("a", "x"), _string("abc"), true, null), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testLikeEscapeParameter() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x LIKE 'abc' ESCAPE :x THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new LikePredicate(path("a", "x"), _string("abc"), true, 'x'), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testLikeEscapeLiteral() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x LIKE 'abc' ESCAPE 'x' THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new LikePredicate(path("a", "x"), _string("abc"), true, 'x'), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testNotLike() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x NOT LIKE 'abc' THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new LikePredicate(path("a", "x"), _string("abc"), true, null, true), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testBetweenArithmetic() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x BETWEEN 1 AND 2 THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new BetweenPredicate(path("a", "x"), _int("1"), _int("2")), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testNotBetweenArithmetic() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x NOT BETWEEN 1 AND 2 THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new BetweenPredicate(path("a", "x"), _int("1"), _int("2"), true), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testBetweenString() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x BETWEEN 'ab' AND 'zb' THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new BetweenPredicate(path("a", "x"), _string("ab"), _string("zb")), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testNotBetweenString() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x NOT BETWEEN 'ab' AND 'zb' THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new BetweenPredicate(path("a", "x"), _string("ab"), _string("zb"), true), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testBetweenCharacter() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x BETWEEN 'a' AND 'z' THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new BetweenPredicate(path("a", "x"), _string("a"), _string("z")), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testNotBetweenCharacter() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x NOT BETWEEN 'a' AND 'z' THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new BetweenPredicate(path("a", "x"), _string("a"), _string("z"), true), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testBetweenDate() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x BETWEEN {d '1991-05-21'} AND {d '1991-05-22'} THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new BetweenPredicate(path("a", "x"), _date(1991, 5, 21), _date(1991, 5, 22)), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testNotBetweenDate() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x NOT BETWEEN {d '1991-05-21'} AND {d '1991-05-22'} THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new BetweenPredicate(path("a", "x"), _date(1991, 5, 21), _date(1991, 5, 22), true), _int("0"))), _int("1"));
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

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new CompoundPredicate(CompoundPredicate.BooleanOperator.OR, new EqPredicate(path("x", "a"), path("y", "a")), new CompoundPredicate(CompoundPredicate.BooleanOperator.AND, new LtPredicate(path("c", "a"), _int("9")), new EqPredicate(subtract(path("b"), path("c")), _int("2")))), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testNot1() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN NOT(x.a = y.a) OR c.a < 9 AND b - c = 2 THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new CompoundPredicate(CompoundPredicate.BooleanOperator.OR, new EqPredicate(path("x", "a"), path("y", "a"), true), new CompoundPredicate(CompoundPredicate.BooleanOperator.AND, new LtPredicate(path("c", "a"), _int("9")), new EqPredicate(subtract(path("b"), path("c")), _int("2")))), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testNot2() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN NOT(localized[:locale].name = localized[:locale].description) THEN 0 ELSE 1 END");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new EqPredicate(path("localized[:locale]", "name"), path("localized[:locale]", "description"), true), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }

    @Test
    public void testNot3() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CaSe WHEN NoT(x.a = y.a OR c.a < 9 and b - c = 2) THeN 0 eLsE 1 ENd");

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(not(new CompoundPredicate(CompoundPredicate.BooleanOperator.OR, new EqPredicate(path("x", "a"), path("y", "a")), new CompoundPredicate(CompoundPredicate.BooleanOperator.AND, new LtPredicate(path("c", "a"), _int("9")), new EqPredicate(subtract(path("b"), path("c")), _int("2"))))), _int("0"))), _int("1"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testBooleanCompare(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN archived = true THEN 1 ELSE 2 END");
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new EqPredicate(path("archived"), _boolean(true)), _int("1"))), _int("2"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testEnumCompare(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN archived = ENUM(a.b.c) THEN 1 ELSE 2 END");
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new EqPredicate(path("archived"), _enum("ENUM(a.b.c)")), _int("1"))), _int("2"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testEntityTypeCompare(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN TYPE(doc) = ENTITY(Document) THEN 1 ELSE 2 END");
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new EqPredicate(typeFunction(path("doc")), _entity("ENTITY(Document)")), _int("1"))), _int("2"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testEntityCompare(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a = b THEN 1 ELSE 2 END");
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new EqPredicate(path("a"), path("b")), _int("1"))), _int("2"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testDateFunctions(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a = b THEN CURRENT_DATE WHEN a > b THEN CURRENT_TIME ELSE CURRENT_TIMESTAMP END");
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(
        		new WhenClauseExpression(new EqPredicate(path("a"), path("b")), function("CURRENT_DATE")),
        		new WhenClauseExpression(new GtPredicate(path("a"), path("b")), function("CURRENT_TIME"))
    		), function("CURRENT_TIMESTAMP")
		);
        assertEquals(expected, result);
    }
    
    @Test
    public void testParameterSignumInvert() {
    	Expression result = parse("-(:test)");
    	assertEquals(new ArithmeticFactor(new ParameterExpression("test"), true), result);
    }
    
    @Test
    public void testConditionalCaseWhen() {
        Predicate result = parseBooleanExpression("CASE WHEN document.age > 12 THEN document.creationDate ELSE CURRENT_TIMESTAMP END < ALL subqueryAlias", true);
        
        Predicate expected = new LtPredicate(
            new GeneralCaseExpression(Arrays.asList(
                    new WhenClauseExpression(new GtPredicate(path("document", "age"), _int("12")), path("document", "creationDate"))
                ), function("CURRENT_TIMESTAMP")
            ),
            foo("subqueryAlias"),
            PredicateQuantifier.ALL,
            false
        );
        assertEquals(expected, result);
    }
}
