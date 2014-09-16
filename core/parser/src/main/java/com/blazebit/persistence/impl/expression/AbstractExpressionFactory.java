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
import java.util.BitSet;
import java.util.logging.Logger;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public abstract class AbstractExpressionFactory implements ExpressionFactory {

    private static final Logger LOG = Logger.getLogger("com.blazebit.persistence.parser");
    
    private Expression createExpression(RuleInvoker ruleInvoker, String expression, boolean allowCaseWhen){
        if (expression == null) {
            throw new NullPointerException("expression");
        }
        if (expression.isEmpty()) {
            throw new IllegalArgumentException("expression");
        }
        JPQLSelectExpressionLexer l = new JPQLSelectExpressionLexer(new ANTLRInputStream(expression));
        configureLexer(l);
        CommonTokenStream tokens = new CommonTokenStream(l);
        JPQLSelectExpressionParser p = new JPQLSelectExpressionParser(tokens, allowCaseWhen);
        configureParser(p);
        ParserRuleContext ctx = ruleInvoker.invokeRule(p);
        LOG.finest(ctx.toStringTree());
        
        JPQLSelectExpressionVisitorImpl visitor = new JPQLSelectExpressionVisitorImpl(tokens);
        return visitor.visit(ctx);
    }
    
    protected abstract RuleInvoker getSimpleExpressionRuleInvoker();
    
    @Override
    public Expression createSimpleExpression(String expression, boolean allowCaseWhen) {
        return createExpression(getSimpleExpressionRuleInvoker(), expression, allowCaseWhen);
    }
    
    @Override
    public Expression createOrderByExpression(String expression) {
        return createExpression(new RuleInvoker() {

            @Override
            public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
                return parser.parseOrderByClause();
            }
        }, expression, false);
    }
    
    @Override
    public Expression createSimpleExpression(String expression) {
        return createSimpleExpression(expression, false);
    }

    @Override
    public Expression createCaseOperandExpression(String expression) {
        return createExpression(new RuleInvoker() {

            @Override
            public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
                return parser.parseCaseOperandExpression();
            }
        }, expression, false);
    }

    @Override
    public Expression createScalarExpression(String expression) {
        return createExpression(new RuleInvoker() {

            @Override
            public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
                return parser.parseScalarExpression();
            }
        }, expression, false);
    }

    @Override
    public Expression createArithmeticExpression(String expression) {
        return createExpression(new RuleInvoker() {

            @Override
            public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
                return parser.parseArithmeticExpression();
            }
        }, expression, false);
    }

    @Override
    public Expression createStringExpression(String expression) {
        return createExpression(new RuleInvoker() {

            @Override
            public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser) {
                return parser.parseStringExpression();
            }
        }, expression, false);
    }
    
    protected void configureLexer(JPQLSelectExpressionLexer lexer){
        lexer.removeErrorListeners();
        lexer.addErrorListener(ERR_LISTENER);
    }
    
    protected void configureParser(JPQLSelectExpressionParser parser){
        parser.removeErrorListeners();
        parser.addErrorListener(ERR_LISTENER);
    }

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
    
    protected interface RuleInvoker {
        public ParserRuleContext invokeRule(JPQLSelectExpressionParser parser);
    }
}
