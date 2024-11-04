/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.datanucleus.function;

import com.blazebit.persistence.spi.JpqlFunction;
import org.datanucleus.store.rdbms.sql.SQLStatement;
import org.datanucleus.store.rdbms.sql.expression.SQLExpression;
import org.datanucleus.store.rdbms.sql.method.SQLMethod;

import java.util.Arrays;

/**
 *
 * @author Christian
 * @since 1.2.0
 */
public class JpqlFunctionSQLMethod extends AbstractJpqlFunctionSQLMethod implements JpqlFunction {

    public JpqlFunctionSQLMethod(SQLStatement stmt, SQLMethod function) {
        super(stmt, function);
    }

    @Override
    protected SQLExpression getExpression(SQLStatement stmt, SQLExpression argumentExpression) {
        return function.getExpression(stmt, null, Arrays.asList(argumentExpression));
    }
}
