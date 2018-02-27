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
        
        if (sfi.getClassMetadata(returnType) != null) {
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
