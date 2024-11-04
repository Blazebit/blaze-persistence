/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate.base.function;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import org.hibernate.MappingException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class HibernateSQLFunctionAdapter implements JpqlFunction {

    private final SessionFactoryImplementor sfi;
    private final SQLFunction function;

    public HibernateSQLFunctionAdapter(SessionFactoryImplementor sfi, SQLFunction function) {
        this.sfi = sfi;
        this.function = function;
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
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        if (firstArgumentType == null) {
            return null;
        }
        Type type = sfi.getTypeHelper().basic(firstArgumentType);
        if (type == null) {
            if (sfi.getEntityPersisters().get(firstArgumentType.getName()) != null) {
                type = sfi.getTypeHelper().entity(firstArgumentType);
            } else {
                try {
                    type = sfi.getTypeHelper().custom(firstArgumentType);
                } catch (MappingException ex) {
                    type = sfi.getTypeHelper().heuristicType(firstArgumentType.getName());
                }
            }
        }

        if (type != null) {
            Type returnType = function.getReturnType(type, sfi);
            if (returnType != null) {
                return returnType.getReturnedClass();
            }
        }

        return null;
    }

    @Override
    public void render(FunctionRenderContext context) {
        throw new UnsupportedOperationException("Rendering functions through this API is not possible!");
    }

}
