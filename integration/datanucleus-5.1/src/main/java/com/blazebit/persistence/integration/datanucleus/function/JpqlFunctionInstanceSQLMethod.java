/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
    protected SQLExpression getExpression(SQLStatement stmt, SQLExpression argumentExpression) {
        return function.getExpression(stmt, argumentExpression, Collections.EMPTY_LIST);
    }
}
