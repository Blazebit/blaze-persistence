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

package com.blazebit.persistence.integration.datanucleus.function;

import com.blazebit.persistence.spi.JpqlFunction;
import org.datanucleus.store.rdbms.sql.SQLStatement;
import org.datanucleus.store.rdbms.sql.expression.SQLExpression;
import org.datanucleus.store.rdbms.sql.method.SQLMethod;

import java.util.Collections;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class JpqlFunctionInstanceSQLMethod extends AbstractJpqlFunctionSQLMethod implements JpqlFunction {

    public JpqlFunctionInstanceSQLMethod(SQLStatement stmt, SQLMethod function) {
        super(stmt, function);
    }

    @Override
    protected SQLExpression getExpression(SQLExpression argumentExpression) {
        return function.getExpression(argumentExpression, Collections.EMPTY_LIST);
    }
}
