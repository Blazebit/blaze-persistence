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

package com.blazebit.persistence.parser.expression;

import com.blazebit.persistence.parser.JPQLSelectExpressionLexer;
import com.blazebit.persistence.parser.JPQLSelectExpressionParser;
import com.blazebit.persistence.parser.predicate.Predicate;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public abstract class AbstractExpressionFactory extends AbstractExpressionFactoryMacroAdapter {

    protected static final Logger LOG = Logger.getLogger("com.blazebit.persistence.parser");

    protected static final ANTLRErrorListener ERR_LISTENER = new ANTLRErrorListener() {

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            throw new SyntaxErrorException("line " + line + ":" + charPositionInLine + " " + msg);
        }

        @Override
        public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
        }

        @Override
        public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
        }

        @Override
        public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
        }
    };

    private static final RuleInvoker PATH_RULE_INVOKER = new RuleInvoker() {

        @Override
        public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
            return parser.parsePath();
        }
    };

    private static final RuleInvoker JOIN_PATH_RULE_INVOKER = new RuleInvoker() {

        @Override
        public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
            return parser.parseJoinPath();
        }
    };

    private static final RuleInvoker ORDER_BY_RULE_INVOKER = new RuleInvoker() {

        @Override
        public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
            return parser.parseOrderByClause();
        }
    };

    private static final RuleInvoker CASE_OPERAND_EXPRESSION_RULE_INVOKER = new RuleInvoker() {

        @Override
        public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
            return parser.parseCaseOperandExpression();
        }
    };

    private static final RuleInvoker SCALAR_EXPRESSION_RULE_INVOKER = new RuleInvoker() {

        @Override
        public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
            return parser.parseScalarExpression();
        }
    };

    private static final RuleInvoker ARITHMETIC_EXPRESSION_RULE_INVOKER = new RuleInvoker() {

        @Override
        public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
            return parser.parseArithmeticExpression();
        }
    };

    private static final RuleInvoker STRING_EXPRESSION_RULE_INVOKER = new RuleInvoker() {

        @Override
        public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
            return parser.parseStringExpression();
        }
    };

    private static final RuleInvoker PREDICATE_EXPRESSION_RULE_INVOKER = new RuleInvoker() {

        @Override
        public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
            return parser.parsePredicateExpression();
        }
    };

    private static final RuleInvoker IN_ITEM_EXPRESSION_RULE_INVOKER = new RuleInvoker() {

        @Override
        public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
            return parser.parseInItemExpression();
        }
    };

    private static final RuleInvoker IN_ITEM_OR_PATH_EXPRESSION_RULE_INVOKER = new RuleInvoker() {

        @Override
        public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
            return parser.parseInItemOrPathExpression();
        }
    };

    private final boolean allowTreatJoinExtension;
    private final boolean optimize;
    private final Set<String> aggregateFunctions;
    private final Map<String, Class<?>> entityTypes;
    private final Map<String, Class<Enum<?>>> enumTypes;
    private final int minEnumSegmentCount;
    private final int minEntitySegmentCount;
    private final ExpressionOptimizer optimizer = new ExpressionOptimizer();

    protected AbstractExpressionFactory(Set<String> aggregateFunctions, Map<String, Class<?>> entityTypes, Map<String, Class<Enum<?>>> enumTypes, boolean allowTreatJoinExtension, boolean optimize) {
        this.aggregateFunctions = aggregateFunctions;
        this.entityTypes = entityTypes;
        this.enumTypes = enumTypes;
        this.allowTreatJoinExtension = allowTreatJoinExtension;
        this.optimize = optimize;

        int minSegmentCount = Integer.MAX_VALUE;
        for (String fqn : enumTypes.keySet()) {
            int count = 1;
            for (int i = 0; i < fqn.length(); i++) {
                if (fqn.charAt(i) == '.') {
                    count++;
                }
            }

            minSegmentCount = Math.min(minSegmentCount, count);
        }
        this.minEnumSegmentCount = minSegmentCount;

        minSegmentCount = Integer.MAX_VALUE;
        if (!entityTypes.isEmpty()) {
            for (String fqn : entityTypes.keySet()) {
                int count = 0;
                for (int i = 0; i < fqn.length(); i++) {
                    if (fqn.charAt(i) == '.') {
                        count++;
                    }
                }

                if (count != 0) {
                    minSegmentCount = Math.min(minSegmentCount, count);
                }
            }
            // If there are entities, but all are in top level packages, there is no min segment count
            if (minSegmentCount == Integer.MAX_VALUE) {
                minSegmentCount = 0;
            }
        }
        this.minEntitySegmentCount = minSegmentCount;
    }

    private Expression createExpression(RuleInvoker ruleInvoker, String expression, boolean allowCaseWhen, boolean allowQuantifiedPredicates, boolean allowTreatJoinExtension, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        if (expression == null) {
            throw new NullPointerException("expression");
        }
        if (expression.isEmpty()) {
            throw new IllegalArgumentException("expression");
        }
        JPQLSelectExpressionLexer l = new JPQLSelectExpressionLexer(new ANTLRInputStream(expression));
        configureLexer(l);
        CommonTokenStream tokens = new CommonTokenStream(l);
        JPQLSelectExpressionParser p = new JPQLSelectExpressionParser(tokens, allowCaseWhen, allowQuantifiedPredicates, allowTreatJoinExtension);
        configureParser(p);
        ParserRuleContext ctx;
        try {
            ctx = ruleInvoker.invokeRule(p);
        } catch (SyntaxErrorException ex) {
            throw new SyntaxErrorException("Could not parse expression '" + expression + "', " + ex.getMessage(), ex);
        }

        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest(ctx.toStringTree());
        }

        JPQLSelectExpressionVisitorImpl visitor = new JPQLSelectExpressionVisitorImpl(aggregateFunctions, enumTypes, entityTypes, minEnumSegmentCount, minEntitySegmentCount, macroConfiguration == null ? Collections.EMPTY_MAP : macroConfiguration.macros, usedMacros);
        Expression parsedExpression = visitor.visit(ctx);
        if (optimize) {
            parsedExpression = parsedExpression.accept(optimizer);
        }
        return parsedExpression;
    }

    protected abstract RuleInvoker getSimpleExpressionRuleInvoker();

    @Override
    public PathExpression createPathExpression(String expression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return (PathExpression) createExpression(PATH_RULE_INVOKER, expression, false, false, false, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createJoinPathExpression(String expression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return createExpression(JOIN_PATH_RULE_INVOKER, expression, false, false, allowTreatJoinExtension, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createOrderByExpression(String expression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return createExpression(ORDER_BY_RULE_INVOKER, expression, false, false, false, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createSimpleExpression(String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return createExpression(getSimpleExpressionRuleInvoker(), expression, true, allowQuantifiedPredicates, false, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createCaseOperandExpression(String expression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return createExpression(CASE_OPERAND_EXPRESSION_RULE_INVOKER, expression, false, false, false, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createScalarExpression(String expression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return createExpression(SCALAR_EXPRESSION_RULE_INVOKER, expression, true, false, false, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createArithmeticExpression(String expression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return createExpression(ARITHMETIC_EXPRESSION_RULE_INVOKER, expression, false, false, false, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createStringExpression(String expression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return createExpression(STRING_EXPRESSION_RULE_INVOKER, expression, false, false, false, macroConfiguration, usedMacros);
    }

    @Override
    public List<Expression> createInItemExpressions(String[] parameterOrLiteralExpressions, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        if (parameterOrLiteralExpressions == null) {
            throw new NullPointerException("parameterOrLiteralExpressions");
        }
        if (parameterOrLiteralExpressions.length == 0) {
            throw new IllegalArgumentException("empty parameterOrLiteralExpressions");
        }

        List<Expression> inItemExpressions = new ArrayList<Expression>();

        if (parameterOrLiteralExpressions.length == 1) {
            inItemExpressions.add(createInItemOrPathExpression(parameterOrLiteralExpressions[0], macroConfiguration, usedMacros));
        } else {
            for (String parameterOrLiteralExpression : parameterOrLiteralExpressions) {
                inItemExpressions.add(createInItemExpression(parameterOrLiteralExpression, macroConfiguration, usedMacros));
            }
        }

        return inItemExpressions;
    }
    
    @Override
    public Predicate createBooleanExpression(String expression, boolean allowQuantifiedPredicates, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return (Predicate) createExpression(PREDICATE_EXPRESSION_RULE_INVOKER, expression, true, allowQuantifiedPredicates, false, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createInItemExpression(String expression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return createExpression(IN_ITEM_EXPRESSION_RULE_INVOKER, expression, false, false, false, macroConfiguration, usedMacros);
    }

    @Override
    public Expression createInItemOrPathExpression(String expression, MacroConfiguration macroConfiguration, Set<String> usedMacros) {
        return createExpression(IN_ITEM_OR_PATH_EXPRESSION_RULE_INVOKER, expression, false, false, false, macroConfiguration, usedMacros);
    }

    protected void configureLexer(JPQLSelectExpressionLexer lexer) {
        lexer.removeErrorListeners();
        lexer.addErrorListener(ERR_LISTENER);
    }

    protected void configureParser(JPQLSelectExpressionParser parser) {
        parser.removeErrorListeners();
        parser.addErrorListener(ERR_LISTENER);
    }

    /**
     *
     * @author Christian Beikov
     * @author Moritz Becker
     * @since 1.0.0
     */
    protected interface RuleInvoker {

        public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser);
    }
}
