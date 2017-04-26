/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.impl.datanucleus.function;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import org.datanucleus.store.rdbms.mapping.java.JavaTypeMapping;
import org.datanucleus.store.rdbms.sql.SQLStatement;
import org.datanucleus.store.rdbms.sql.expression.BooleanExpression;
import org.datanucleus.store.rdbms.sql.expression.ByteExpression;
import org.datanucleus.store.rdbms.sql.expression.CharacterExpression;
import org.datanucleus.store.rdbms.sql.expression.NumericExpression;
import org.datanucleus.store.rdbms.sql.expression.SQLExpression;
import org.datanucleus.store.rdbms.sql.expression.StringExpression;
import org.datanucleus.store.rdbms.sql.expression.TemporalExpression;
import org.datanucleus.store.rdbms.sql.method.SQLMethod;

import java.util.logging.Logger;

/**
 *
 * @author Christian
 * @since 1.2.0
 */
public abstract class AbstractJpqlFunctionSQLMethod implements JpqlFunction {

    private static final Logger LOG = Logger.getLogger(JpqlFunctionSQLMethod.class.getName());
    protected final SQLStatement stmt;
    protected final SQLMethod function;

    public AbstractJpqlFunctionSQLMethod(SQLStatement stmt, SQLMethod function) {
        this.stmt = stmt;
        this.function = function;
    }

    @Override
    public boolean hasArguments() {
        // Not sure how to determine that
        return true;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        // Not sure how to determine that
        return true;
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        if (firstArgumentType == null) {
            return null;
        }
        SQLExpression expression;
        JavaTypeMapping argumentTypeMapping = stmt.getSQLExpressionFactory().getMappingForType(firstArgumentType, true);
        if (java.sql.Date.class.isAssignableFrom(firstArgumentType)) {
            expression = new TemporalExpression(stmt, argumentTypeMapping, "", null);
        } else if (Byte.class.isAssignableFrom(firstArgumentType)) {
            expression = new ByteExpression(stmt, null, argumentTypeMapping);
        } else if (Number.class.isAssignableFrom(firstArgumentType)) {
            expression = new NumericExpression(stmt, argumentTypeMapping, "");
        } else if (String.class.isAssignableFrom(firstArgumentType)) {
            expression = new StringExpression(stmt, null, argumentTypeMapping);
        } else if (Character.class.isAssignableFrom(firstArgumentType)) {
            expression = new CharacterExpression(stmt, null, argumentTypeMapping);
        } else if (Boolean.class.isAssignableFrom(firstArgumentType)) {
            expression = new BooleanExpression(stmt, argumentTypeMapping, "");
        } else {
            throw new UnsupportedOperationException("Unsupported data type: " + firstArgumentType.getName());
        }
        // Well, since the method is a singleton object doing this is actually a concurrency problem
        // But when you look into the datanucleus code it has many data races so you are probably used to it anyway
        function.setStatement(stmt);
        SQLExpression resultExpression = getExpression(expression);
        return resultExpression.getJavaTypeMapping().getJavaType();
    }

    protected abstract SQLExpression getExpression(SQLExpression argumentExpression);

    @Override
    public void render(FunctionRenderContext context) {
        throw new UnsupportedOperationException("Rendering functions through this API is not possible!");
    }
}