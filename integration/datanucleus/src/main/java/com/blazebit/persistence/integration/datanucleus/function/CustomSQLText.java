/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.datanucleus.function;

import org.datanucleus.store.rdbms.sql.SQLText;
import org.datanucleus.store.rdbms.sql.expression.SQLExpression;

import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CustomSQLText extends SQLText {

    private final String sql;

    public CustomSQLText(String sql, SQLExpression expr, List<SQLExpression> expressions) {
        this.sql = sql;
        
        if (expr != null) {
            append(expr);
        }

        for (SQLExpression expression : expressions) {
            append(expression);
        }
    }

    @Override
    public String toSQL() {
        // Call super to trigger parameter handling
        super.toSQL();
        return sql;
    }
    
}
