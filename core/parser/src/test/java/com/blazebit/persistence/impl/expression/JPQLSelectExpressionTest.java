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

import com.blazebit.persistence.parser.JPQLSelectExpressionLexer;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
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

    private static final Logger LOG = Logger.getLogger("com.blazebit.persistence.parser");

    @BeforeClass
    public static void initLogging() {
        try {
            LogManager.getLogManager().readConfiguration(JPQLSelectExpressionTest.class.getResourceAsStream(
                "/logging.properties"));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private CompositeExpression parse(String expr, boolean allowCaseWhen) {
        JPQLSelectExpressionLexer l = new JPQLSelectExpressionLexer(new ANTLRInputStream(expr));
        BufferedTokenStream tokens = new BufferedTokenStream(l);
        JPQLSelectExpressionParser p = new JPQLSelectExpressionParser(tokens, allowCaseWhen);
        p.setTrace(LOG.isLoggable(Level.FINEST));
        JPQLSelectExpressionParser.ParseSimpleExpressionContext ctx = p.parseSimpleExpression();

        LOG.finest(ctx.toStringTree());
        ParseTreeWalker w = new ParseTreeWalker();

        JPQLSelectExpressionVisitorImpl visitor = new JPQLSelectExpressionVisitorImpl(tokens);
        return (CompositeExpression) visitor.visit(ctx);
        
//        JPQLSelectExpressionListenerImpl listener = new JPQLSelectExpressionListenerImpl(tokens);
//        w.walk(listener, ctx);

//        return listener.getCompositeExpression();
    }

    private FunctionExpression function(String name, Expression... args){
        return new FunctionExpression(name, Arrays.asList(args));
    }
    
    private AggregateExpression aggregate(String name, PathExpression arg, boolean distinct){
        return new AggregateExpression(distinct, name, arg);
    }
    
    private AggregateExpression aggregate(String name, PathExpression arg){
        return new AggregateExpression(false, name, arg);
    }
    
    private CompositeExpression parse(String expr) {
        return parse(expr, false);
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
    
    private ParameterExpression parameter(String name){
        return new ParameterExpression(name);
    }

    @Test
    public void testSizeFunction(){
        CompositeExpression result = parse("SIZE(d.contacts)");
        List<Expression> expressions = result.getExpressions();
        
        assertTrue(expressions.size() == 1);
        assertEquals(function("SIZE", path("d", "contacts")), expressions.get(0));
    }
    
    @Test
    public void testAggregateExpressionSinglePath() {
        CompositeExpression result = parse("AVG(age)");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertEquals(aggregate("AVG", path("age")), expressions.get(0));
    }

    @Test
    public void testAggregateExpressionMultiplePath() {
        CompositeExpression result = parse("AVG(d.age)");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertEquals(function("AVG", path("d", "age")), expressions.get(0));
    }

    @Test
    public void testParser2() {
        CompositeExpression result = parse("d.problem.age");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("d", "problem", "age")));
    }

    @Test
    public void testParser3() {
        CompositeExpression result = parse("age");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("age")));
    }

    @Test
    public void testParserArithmetic() {
        CompositeExpression result = parse("d.age + SUM(d.children.age)");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 3);

        assertTrue(expressions.get(0).equals(path("d", "age")));
        assertTrue(expressions.get(1).equals(new FooExpression(" + ")));
        assertTrue(expressions.get(2).equals(function("SUM", path("d", "children", "age"))));
    }

    @Test
    public void testParserArithmetic2() {
        CompositeExpression result = parse("age + 1");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 2);

        assertTrue(expressions.get(0).equals(path("age")));
        assertTrue(expressions.get(1).equals(new FooExpression(" + 1")));
    }

    @Test
    public void testNullLiteralExpression() {
        CompositeExpression result = parse("NULLIF(1,1)");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);

        assertTrue(expressions.get(0).equals(function("NULLIF", new FooExpression("1"), new FooExpression("1"))));
    }

    @Test
    public void testCountIdExpression() {
        CompositeExpression result = parse("COUNT(id)");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(function("COUNT", path("id"))));
    }

    @Test
    public void testKeyMapExpression() {
        CompositeExpression result = parse("KEY(map)");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(function("KEY", path("map"))));
    }

    @Test
    public void testArrayExpression() {
        CompositeExpression result = parse("versions[test]");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("versions[test]")));
    }

    @Test
    public void testArrayIndexPath() {
        CompositeExpression result = parse("versions[test.x.y]");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("versions[test.x.y]")));
    }

    @Test
    public void testArrayIndexArithmetic() {
        CompositeExpression result = parse("versions[test.x.y + test.b]");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);

        PathExpression expected = new PathExpression();
        List<Expression> compositeExpressions = new ArrayList<Expression>();
        compositeExpressions.add(path("test", "x", "y"));
        compositeExpressions.add(new FooExpression(" + "));
        compositeExpressions.add(path("test", "b"));
        CompositeExpression expectedIndex = new CompositeExpression(compositeExpressions);
        expected.getExpressions().add(new ArrayExpression(new PropertyExpression("versions"), expectedIndex));
        assertTrue(expressions.get(0).equals(expected));
    }

    @Test
    public void testArrayIndexArithmeticMixed() {
        CompositeExpression result = parse("versions[test.x.y + 1]");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);

        PathExpression expected = new PathExpression();
        List<Expression> compositeExpressions = new ArrayList<Expression>();
        compositeExpressions.add(path("test", "x", "y"));
        compositeExpressions.add(new FooExpression(" + 1"));
        CompositeExpression expectedIndex = new CompositeExpression(compositeExpressions);
        expected.getExpressions().add(new ArrayExpression(new PropertyExpression("versions"), expectedIndex));

        assertTrue(expressions.get(0).equals(expected));
    }

    @Test
    public void testArrayIndexArithmeticLiteral() {
        CompositeExpression result = parse("versions[2 + 1]");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);

        PathExpression expected = new PathExpression();
        expected.getExpressions().add(new ArrayExpression(new PropertyExpression("versions"), new FooExpression("2 + 1")));

        assertTrue(expressions.get(0).equals(expected));
    }

    @Test
    public void testMultipleArrayExpressions() {
        CompositeExpression result = parse("versions[test.x.y].owner[a.b.c]");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);

        assertTrue(expressions.get(0).equals(path("versions[test.x.y]", "owner[a.b.c]")));
    }

    @Test
    public void testArrayInTheMiddle() {
        CompositeExpression result = parse("owner.versions[test.x.y].test");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);

        assertTrue(expressions.get(0).equals(path("owner", "versions[test.x.y]", "test")));
    }

    @Test
    public void testArrayWithParameterIndex() {
        CompositeExpression result = parse("versions[:index]");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("versions[:index]")));
    }

    @Test(expected = SyntaxErrorException.class)
    public void testArrayWithInvalidParameterIndex() {
        parse("versions[:index.b]");
    }

    @Test
    public void testSingleElementArrayIndexPath1() {
        CompositeExpression result = parse("d[a]");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("d[a]")));
    }

    @Test
    public void testSingleElementArrayIndexPath2() {
        CompositeExpression result = parse("d.b[a]");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("d", "b[a]")));
    }

    @Test
    public void testSingleElementArrayIndexPath3() {
        CompositeExpression result = parse("d.b[a].c");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("d", "b[a]", "c")));
    }

    @Test
    public void testSingleElementArrayIndexPath4() {
        CompositeExpression result = parse("d.b[a].c[e]");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("d", "b[a]", "c[e]")));
    }

    @Test
    public void testSingleElementArrayIndexPath5() {
        CompositeExpression result = parse("d.b[a].c[e].f");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("d", "b[a]", "c[e]", "f")));
    }

    @Test
    public void testMultiElementArrayIndexPath1() {
        CompositeExpression result = parse("d[a.a]");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("d[a.a]")));
    }

    @Test
    public void testMultiElementArrayIndexPath2() {
        CompositeExpression result = parse("d.b[a.a]");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("d", "b[a.a]")));
    }

    @Test
    public void testMultiElementArrayIndexPath3() {
        CompositeExpression result = parse("d.b[a.a].c");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("d", "b[a.a]", "c")));
    }

    @Test
    public void testMultiElementArrayIndexPath4() {
        CompositeExpression result = parse("d.b[a.a].c[e.a]");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("d", "b[a.a]", "c[e.a]")));
    }

    @Test
    public void testMultiElementArrayIndexPath5() {
        CompositeExpression result = parse("d.b[a.a].c[e.a].f");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("d", "b[a.a]", "c[e.a]", "f")));
    }

    @Test
    public void testParameterArrayIndex1() {
        CompositeExpression result = parse("d[:a]");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("d[:a]")));
    }

    @Test
    public void testParameterArrayIndex2() {
        CompositeExpression result = parse("d.b[:a]");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("d", "b[:a]")));
    }

    @Test
    public void testParameterArrayIndex3() {
        CompositeExpression result = parse("d.b[:a].c");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("d", "b[:a]", "c")));
    }

    @Test
    public void testParameterArrayIndex4() {
        CompositeExpression result = parse("d.b[:a].c[:a]");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("d", "b[:a]", "c[:a]")));
    }

    @Test
    public void testParameterArrayIndex5() {
        CompositeExpression result = parse("d.b[:a].c[:a].f");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(path("d", "b[:a]", "c[:a]", "f")));
    }

    @Test
    public void testKeyFunctionArray() {
        CompositeExpression result = parse("KEY(localized[:locale])");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(function("KEY", path("localized[:locale]"))));
    }
    
    @Test
    public void testKeyFunctionPath() {
        CompositeExpression result = parse("KEY(d.age)");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(function("KEY", path("d", "age"))));
    }

    @Test
    public void testValueFunctionArray() {
        CompositeExpression result = parse("VALUE(localized[:locale])");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(function("VALUE", path("localized[:locale]"))));
    }
    
    @Test
    public void testValueFunctionPath() {
        CompositeExpression result = parse("VALUE(d.age)");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(function("VALUE", path("d", "age"))));
    }

    @Test
    public void testEntryFunctionArray() {
        CompositeExpression result = parse("ENTRY(localized[:locale])");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(function("ENTRY", path("localized[:locale]"))));
    }
    
    @Test
    public void testEntryFunctionPath() {
        CompositeExpression result = parse("ENTRY(d.age)");
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(function("ENTRY", path("d", "age"))));
    }
    
    @Test
    public void testCaseWhenSwitchTrue() {
        CompositeExpression result = parse("CASE WHEN KEY(localized[:locale]) NOT MEMBER OF supportedLocales THEN true ELSE false END", true);
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 5);
        assertTrue(expressions.get(0).equals(new FooExpression("CASE WHEN ")));
        assertTrue(expressions.get(1).equals(function("KEY", path("localized[:locale]"))));
        assertTrue(expressions.get(2).equals(new FooExpression(" NOT MEMBER OF ")));
        assertTrue(expressions.get(3).equals(path("supportedLocales")));
        assertTrue(expressions.get(4).equals(new FooExpression(" THEN true ELSE false END")));
    }

    @Test(expected = SyntaxErrorException.class)
    public void testCaseWhenSwitchFalse() {
        CompositeExpression result = parse("CASE WHEN KEY(localized[:locale]) NOT MEMBER OF supportedLocales THEN true ELSE false END", false);
        List<Expression> expressions = result.getExpressions();

        assertTrue(expressions.size() == 5);
        assertTrue(expressions.get(0).equals(new FooExpression("CASE WHEN ")));
        assertTrue(expressions.get(1).equals(function("KEY", path("localized[:locale]"))));
        assertTrue(expressions.get(2).equals(new FooExpression(" NOT MEMBER OF ")));
        assertTrue(expressions.get(3).equals(path("supportedLocales")));
        assertTrue(expressions.get(4).equals(new FooExpression(" THEN true ELSE false END")));
    }
    
    @Test
    public void testTypeFunctionPath(){
        CompositeExpression result = parse("TYPE(d.age)");
        List<Expression> expressions = result.getExpressions();
        
        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(function("TYPE", path("d", "age"))));
    }
    
    @Test
    public void testTypeFunctionParameter(){
        CompositeExpression result = parse("TYPE(:test)");
        List<Expression> expressions = result.getExpressions();
        
        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(function("TYPE", parameter("test"))));
    }
    
    @Test
    public void testTypeFunctionSingleElementPath(){
        CompositeExpression result = parse("TYPE(age)");
        List<Expression> expressions = result.getExpressions();
        
        assertTrue(expressions.size() == 1);
        assertTrue(expressions.get(0).equals(function("TYPE", path("age"))));
    }
}
