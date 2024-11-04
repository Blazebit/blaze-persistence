/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser;

import com.blazebit.persistence.parser.expression.AbstractExpressionFactory;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import org.antlr.v4.runtime.atn.PredictionMode;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Moritz Becker
 */
public abstract class AbstractTestExpressionFactory extends AbstractExpressionFactory {

    public AbstractTestExpressionFactory(Map<String, FunctionKind> functions, boolean optimize) {
        this(functions, Collections.EMPTY_MAP, Collections.EMPTY_MAP, optimize);
    }
    
    public AbstractTestExpressionFactory(Map<String, FunctionKind> functions, Map<String, Class<?>> entityTypes, Map<String, Class<Enum<?>>> enumTypes, boolean optimize) {
        super(functions, entityTypes, enumTypes, enumTypes, optimize);
    }

    public AbstractTestExpressionFactory(Map<String, FunctionKind> functions, Map<String, Class<?>> entityTypes, Map<String, Class<Enum<?>>> enumTypes, Map<String, Class<Enum<?>>> enumTypesForLiterals, boolean optimize) {
        super(functions, entityTypes, enumTypes, enumTypesForLiterals, optimize);
    }

    @Override
    protected void configureLexer(JPQLNextLexer lexer) {
        lexer.removeErrorListeners();
        lexer.addErrorListener(ERR_LISTENER);
    }

    @Override
    protected void configureParser(JPQLNextParser parser) {
        if (LOG.isLoggable(Level.FINEST)) {
            parser.setTrace(true);
        }
        
        parser.removeErrorListeners();
        parser.addErrorListener(ERR_LISTENER);
        parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
    }

    @Override
    public <T extends ExpressionFactory> T unwrap(Class<T> clazz) {
        if (clazz.isAssignableFrom(AbstractTestExpressionFactory.class)) {
            return (T) this;
        }
        return null;
    }
}
