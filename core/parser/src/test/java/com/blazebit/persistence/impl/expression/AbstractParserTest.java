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

import com.blazebit.persistence.impl.predicate.NotPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.LogManager;
import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.BeforeClass;

/**
 *
 * @author Moritz Becker
 */
public class AbstractParserTest {

    protected ExpressionFactory ef = new AbstractTestExpressionFactory() {

        private final AbstractExpressionFactory.RuleInvoker simpleExpressionRuleInvoker = new AbstractExpressionFactory.RuleInvoker() {

            @Override
            public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
                return parser.parseSimpleExpression();
            }
        };

        @Override
        protected AbstractExpressionFactory.RuleInvoker getSimpleExpressionRuleInvoker() {
            return simpleExpressionRuleInvoker;
        }

    };
    protected ExpressionFactory subqueryEf = new AbstractTestExpressionFactory() {

        private final AbstractExpressionFactory.RuleInvoker simpleExpressionRuleInvoker = new AbstractExpressionFactory.RuleInvoker() {

            @Override
            public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
                return parser.parseSimpleSubqueryExpression();
            }
        };

        @Override
        protected AbstractExpressionFactory.RuleInvoker getSimpleExpressionRuleInvoker() {
            return simpleExpressionRuleInvoker;
        }

    };

    @BeforeClass
    public static void initLogging() {
        try {
            LogManager.getLogManager().readConfiguration(GeneralParserTest.class.getResourceAsStream(
                    "/logging.properties"));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    protected NotPredicate not(Predicate p) {
        return new NotPredicate(p);
    }

    protected CompositeExpression compose(Expression... expr) {
        return new CompositeExpression(Arrays.asList(expr));
    }

    protected Expression parseOrderBy(String expr) {
        return ef.createOrderByExpression(expr);
    }
    
    protected Expression parseArithmeticExpr(String expr) {
        return ef.createArithmeticExpression(expr);
    }
    
    protected Expression parseStringExpr(String expr) {
        return ef.createStringExpression(expr);
    }

    protected Expression parse(String expr) {
        return parse(expr, false);
    }

    protected Expression parse(String expr, boolean allowCaseWhen) {
        return ef.createSimpleExpression(expr, allowCaseWhen);
    }

    protected Expression parseSubqueryExpression(String expr) {
        return parseSubqueryExpression(expr, false);
    }

    protected Expression parseSubqueryExpression(String expr, boolean allowCaseWhen) {
        return subqueryEf.createSimpleExpression(expr, allowCaseWhen);
    }

    protected FooExpression foo(String foo) {
        return new FooExpression(foo);
    }

    protected FunctionExpression function(String name, Expression... args) {
        return new FunctionExpression(name, Arrays.asList(args));
    }

    protected AggregateExpression aggregate(String name, PathExpression arg, boolean distinct) {
        return new AggregateExpression(distinct, name, arg);
    }

    protected AggregateExpression aggregate(String name, PathExpression arg) {
        return new AggregateExpression(false, name, arg);
    }

    protected PathExpression path(String... properties) {
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

    protected ArrayExpression array(String expr) {
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

    protected ParameterExpression parameter(String name) {
        return new ParameterExpression(name);
    }
}
