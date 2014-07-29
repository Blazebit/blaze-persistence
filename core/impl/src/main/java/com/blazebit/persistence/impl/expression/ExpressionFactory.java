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
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 *
 * @author Moritz Becker
 */
public final class ExpressionFactory {

    

    public Expression createSimpleExpression(String expression) {
        if (expression == null) {
            throw new NullPointerException("expression");
        }
        if (expression.isEmpty()) {
            throw new IllegalArgumentException("expression");
        }
        JPQLSelectExpressionLexer l = new JPQLSelectExpressionLexer(new ANTLRInputStream(expression));
        l.addErrorListener(errorListener);
        CommonTokenStream tokens = new CommonTokenStream(l);
        JPQLSelectExpressionParser p = new JPQLSelectExpressionParser(tokens);
        p.addErrorListener(errorListener);
        JPQLSelectExpressionParser.ParseSimpleExpressionContext ctx = p.parseSimpleExpression();

        ParseTreeWalker w = new ParseTreeWalker();

        JPQLSelectExpressionListenerImpl antlrToExpressionTransformer = new JPQLSelectExpressionListenerImpl();

        w.walk(antlrToExpressionTransformer, ctx);

        CompositeExpression expr = antlrToExpressionTransformer.getCompositeExpression();

        // unwrap composite expression with single child
        if (expr.getExpressions()
                .size() == 1) {
            return expr.getExpressions()
                    .get(0);
        }
        return expr;
    }

    public Expression createCaseOperandExpression(String caseOperandExpression) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Expression createScalarExpression(String expression) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private static final ANTLRErrorListener errorListener = new ANTLRErrorListener() {

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
}
