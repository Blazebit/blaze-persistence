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

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
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
            if (sfi.getClassMetadata(firstArgumentType) != null) {
                type = sfi.getTypeHelper().entity(firstArgumentType);
            } else {
                type = sfi.getTypeHelper().custom(firstArgumentType);
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
