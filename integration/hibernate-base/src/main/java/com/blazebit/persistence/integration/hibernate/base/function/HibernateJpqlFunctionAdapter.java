/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate.base.function;

import com.blazebit.persistence.spi.JpqlFunction;
import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class HibernateJpqlFunctionAdapter implements SQLFunction {
    
    private final JpqlFunction function;

    public HibernateJpqlFunctionAdapter(JpqlFunction function) {
        this.function = function;
    }

    public JpqlFunction unwrap() {
        return function;
    }

    @Override
    public boolean hasArguments() {
        return function.hasArguments();
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return function.hasParenthesesIfNoArguments();
    }

    @Override
    public Type getReturnType(Type firstArgumentType, Mapping mapping) throws QueryException {
        SessionFactoryImplementor sfi = (SessionFactoryImplementor) mapping;
        Class<?> argumentClass;
        
        if (firstArgumentType == null) {
            argumentClass = null;
        } else {
            argumentClass = firstArgumentType.getReturnedClass();
        }
        
        Class<?> returnType = function.getReturnType(argumentClass);
        
        if (returnType == null) {
            return null;
        } else if (argumentClass == returnType) {
            return firstArgumentType;
        }
        
        Type type = sfi.getTypeHelper().basic(returnType);
        
        if (type != null) {
            return type;
        }

        if (sfi.getEntityPersisters().containsKey(returnType.getName())) {
            return sfi.getTypeHelper().entity(returnType);
        }

        return sfi.getTypeHelper().custom(returnType);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String render(Type firstArgumentType, List args, SessionFactoryImplementor factory) throws QueryException {
        HibernateFunctionRenderContext context = new HibernateFunctionRenderContext(args);
        function.render(context);
        return context.renderToString();
    }
}
