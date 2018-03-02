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
