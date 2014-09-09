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

import com.blazebit.persistence.parser.JPQLSelectExpressionParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.LogManager;
import javax.persistence.QueryTimeoutException;
import org.antlr.v4.runtime.ParserRuleContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class JPQLSelectExpressionTest {

    private ExpressionFactory ef = new AbstractTestExpressionFactory() {

        @Override
        protected ParserRuleContext callStartRule(JPQLSelectExpressionParser parser) {
            return parser.parseSimpleExpression();
        }

    };
    private ExpressionFactory subqueryEf = new AbstractTestExpressionFactory() {

        @Override
        protected ParserRuleContext callStartRule(JPQLSelectExpressionParser parser) {
            return parser.parseSimpleSubqueryExpression();
        }

    };

    @BeforeClass
    public static void initLogging() {
        try {
            LogManager.getLogManager().readConfiguration(JPQLSelectExpressionTest.class.getResourceAsStream(
                    "/logging.properties"));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private CompositeExpression compose(Expression... expr) {
        return new CompositeExpression(Arrays.asList(expr));
    }

    private Expression parseOrderBy(String expr){
        return ef.createOrderByExpression(expr);
    }
    
    private Expression parse(String expr) {
        return parse(expr, false);
    }

    private Expression parse(String expr, boolean allowCaseWhen) {
        return ef.createSimpleExpression(expr, allowCaseWhen);
    }

    private Expression parseSubqueryExpression(String expr) {
        return parseSubqueryExpression(expr, false);
    }

    private Expression parseSubqueryExpression(String expr, boolean allowCaseWhen) {
        return subqueryEf.createSimpleExpression(expr, allowCaseWhen);
    }
    
    private FooExpression foo(String foo){
        return new FooExpression(foo);
    }
    
    private FunctionExpression function(String name, Expression... args) {
        return new FunctionExpression(name, Arrays.asList(args));
    }

    private AggregateExpression aggregate(String name, PathExpression arg, boolean distinct) {
        return new AggregateExpression(distinct, name, arg);
    }

    private AggregateExpression aggregate(String name, PathExpression arg) {
        return new AggregateExpression(false, name, arg);
    }

    private PathExpression path(String... properties) {
        PathExpression p = new PathExpression(new ArrayList<PathElementExpression>());
        for (String pathElem : properties) {
            if (pathElem.contains("[")) {
                p.getExpressions().add(array(pathElem));
            } else {
                p.getExpressions().add(new PropertyExpression(pathElem));
            }
        }
        return p;
    }

    private ArrayExpression array(String expr) {
        int firstIndex = expr.indexOf('[');
        int lastIndex = expr.indexOf(']');
        String base = expr.substring(0, firstIndex);
        String index = expr.substring(firstIndex + 1, lastIndex);
        Expression indexExpr;
        if (index.startsWith(":")) {
            indexExpr = new ParameterExpression(index.substring(1));
        } else {
            indexExpr = path(index.split("\\."));
        }
        return new ArrayExpression(new PropertyExpression(base), indexExpr);
    }

    private ParameterExpression parameter(String name) {
        return new ParameterExpression(name);
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
    public void testParserArithmetic() {
        CompositeExpression result = (CompositeExpression) parse("d.age + SUM(d.children.age)");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 3);

        assertTrue(expressions.get(0).equals(path("d", "age")));
        assertTrue(expressions.get(1).equals(new FooExpression(" + ")));
        assertTrue(expressions.get(2).equals(aggregate("SUM", path("d", "children", "age"))));
    }

    @Test
    public void testParserArithmetic2() {
        CompositeExpression result = (CompositeExpression) parse("age + 1");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 2);

        assertTrue(expressions.get(0).equals(path("age")));
        assertTrue(expressions.get(1).equals(new FooExpression(" + 1")));
    }

    @Test
    public void testParserArithmetic3() {
        CompositeExpression result = (CompositeExpression) parse("age * 1");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 2);

        assertEquals(path("age"), expressions.get(0));
        assertEquals(new FooExpression(" * 1"), expressions.get(1));
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

    @Test
    public void testCaseWhenSwitchTrue() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN localized[:locale] NOT MEMBER OF supportedLocales THEN true ELSE false END", true);
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(
                new WhenClauseExpression(compose(path("localized[:locale]"), foo(" NOT MEMBER OF "), path("supportedLocales")), foo("true"))),
                foo("false"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testCaseWhenMultipleWhenClauses() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x = 2 THEN true WHEN a.x = 3 THEN false ELSE false END", true);
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(
                new WhenClauseExpression(compose(path("a", "x"), foo(" = 2")), foo("true")),
                new WhenClauseExpression(compose(path("a", "x"), foo(" = 3")), foo("false"))
        ), foo("false"));
        
        assertEquals(expected, result);
    }

    @Test(expected = SyntaxErrorException.class)
    public void testCaseWhenSwitchFalse() {
        parse("CASE WHEN KEY(localized[:locale]) NOT MEMBER OF supportedLocales THEN true ELSE false END", false);
    }

    @Test
    public void testCaseWhenSize() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN SIZE(d.contacts) > 2 THEN 2 ELSE SIZE(d.contacts) END", true);

        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(function("SIZE", path("d", "contacts")), foo(" > 2")), foo("2"))) , function("SIZE", path("d", "contacts")));
        assertEquals(expected, result);
    }

    @Test
    public void testMemberOf() {
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN x.a MEMBER OF y.a THEN 0 ELSE 2 END", true);

        CompositeExpression condition = compose(path("x", "a"), new FooExpression(" MEMBER OF "), path("y", "a"));
        GeneralCaseExpression expectedExpr = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(condition, new FooExpression("0"))), new FooExpression("2"));
        assertEquals(expectedExpr, result);
    }

    @Test(expected = SyntaxErrorException.class)
    public void testMemberOfInvalidUse() {
        parse("x.a MEMBER OF y.a", true);
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
        assertEquals(function("FUNCTION", new FooExpression("'myfunc'"), path("a", "b"), new FooExpression("'b'"), new FooExpression("12")), result);
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
    public void testCoalesce() {
        Expression result = parseSubqueryExpression("COALESCE(a.b.c, a.b, a.a, 'da', 1)");
        assertEquals(function("COALESCE", path("a", "b", "c"), path("a", "b"), path("a", "a"), new FooExpression("'da'"), new FooExpression("1")), result);
    }
    
    @Test
    public void testInParameter(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x IN (:abc) THEN 0 ELSE 1 END", true);
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(path("a", "x"), foo(" IN ("), new ParameterExpression("abc"), foo(")")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testInParameterNoParanthesis(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x IN :abc THEN 0 ELSE 1 END", true);
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(path("a", "x"), foo(" IN "), new ParameterExpression("abc")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testInNumericLiterals(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x IN (1, 2, 3, 4) THEN 0 ELSE 1 END", true);
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(path("a", "x"), foo(" IN (1,2,3,4)")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testInCharacterLiterals(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x IN ('1', '2', '3', '4') THEN 0 ELSE 1 END", true);
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(path("a", "x"), foo(" IN ('1','2','3','4')")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testIsNull(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x IS NULL THEN 0 ELSE 1 END", true);
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(path("a", "x"), foo(" IS NULL")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testIsNotNull(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x IS NOT NULL THEN 0 ELSE 1 END", true);
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(path("a", "x"), foo(" IS NOT NULL")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testLike(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x LIKE 'abc' THEN 0 ELSE 1 END", true);
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(path("a", "x"), foo(" LIKE 'abc'")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testLikeEscapeParameter(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x LIKE 'abc' ESCAPE :x THEN 0 ELSE 1 END", true);
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(path("a", "x"), foo(" LIKE 'abc' ESCAPE "), new ParameterExpression("x")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testLikeEscapeLiteral(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x LIKE 'abc' ESCAPE 'x' THEN 0 ELSE 1 END", true);
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(path("a", "x"), foo(" LIKE 'abc' ESCAPE 'x'")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testNotLike(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x NOT LIKE 'abc' THEN 0 ELSE 1 END", true);
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(path("a", "x"), foo(" NOT LIKE 'abc'")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testBetweenArithmetic(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x BETWEEN 1 AND 2 THEN 0 ELSE 1 END", true);
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(path("a", "x"), foo(" BETWEEN 1 AND 2")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testNotBetweenArithmetic(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x NOT BETWEEN 1 AND 2 THEN 0 ELSE 1 END", true);
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(path("a", "x"), foo(" NOT BETWEEN 1 AND 2")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testBetweenString(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x BETWEEN 'ab' AND 'zb' THEN 0 ELSE 1 END", true);
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(path("a", "x"), foo(" BETWEEN 'ab' AND 'zb'")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testNotBetweenString(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x NOT BETWEEN 'ab' AND 'zb' THEN 0 ELSE 1 END", true);
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(path("a", "x"), foo(" NOT BETWEEN 'ab' AND 'zb'")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testBetweenCharacter(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x BETWEEN 'a' AND 'z' THEN 0 ELSE 1 END", true);
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(path("a", "x"), foo(" BETWEEN 'a' AND 'z'")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testNotBetweenCharacter(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x NOT BETWEEN 'a' AND 'z' THEN 0 ELSE 1 END", true);
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(path("a", "x"), foo(" NOT BETWEEN 'a' AND 'z'")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testBetweenDate(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x BETWEEN (d '1991-05-21') AND (d '1991-05-22') THEN 0 ELSE 1 END", true);
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(path("a", "x"), foo(" BETWEEN (d '1991-05-21') AND (d '1991-05-22')")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
    
    @Test
    public void testNotBetweenDate(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN a.x NOT BETWEEN (d '1991-05-21') AND (d '1991-05-22') THEN 0 ELSE 1 END", true);
        
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(path("a", "x"), foo(" NOT BETWEEN (d '1991-05-21') AND (d '1991-05-22')")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
    
    @Test(expected = SyntaxErrorException.class)
    public void testOrderByParsing1(){
        parseOrderBy("a.b + b.c");
    }
    
    @Test(expected = SyntaxErrorException.class)
    public void testOrderByParsing2(){
        parseOrderBy("SIZE(a.b)");
    }
    
    @Test
    public void testCaseWhenAndOr(){
        GeneralCaseExpression result = (GeneralCaseExpression) parse("CASE WHEN x.a = y.a OR c.a < 9 AND b - c = 2 THEN 0 ELSE 1 END", true);
    
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(compose(path("x", "a"), foo(" = "), path("y", "a"), foo(" OR "), path("c", "a"), foo(" < 9 AND "), path("b"), foo(" - "), path("c"), foo(" = 2")), foo("0"))), foo("1"));
        assertEquals(expected, result);
    }
}
