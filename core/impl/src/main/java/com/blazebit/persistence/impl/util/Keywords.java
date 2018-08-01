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

package com.blazebit.persistence.impl.util;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

/**
 * @author Giovanni Lovato
 * @since 1.3.0
 */
public final class Keywords {

    /**
     * JPQL keywords from JPA BNF.
     */
    public static final Set<String> JPQL = new HashSet<>(Arrays
        .asList("ABS", "ALL", "AND", "ANY", "AS", "ASC", "AVG", "BETWEEN", "BOTH", "BY", "CASE",
            "COALESCE", "CONCAT", "COUNT", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "DELETE", "DESC", "DISTINCT",
            "ELSE", "EMPTY", "END", "ENTRY", "ESCAPE", "EXISTS", "FETCH", "FROM", "FUNCTION", "GROUP", "HAVING", "IN", "INDEX",
            "INNER", "IS", "JOIN", "KEY", "LEADING", "LEFT", "LENGTH", "LIKE", "LOCATE", "LOWER", "MAX", "MEMBER", "MIN", "MOD",
            "NEW", "NOT", "NULL", "NULLIF", "OBJECT", "OF", "ON", "OR", "ORDER", "OUTER", "SELECT", "SET", "SIZE", "SOME",
                "SQRT", "SUBSTRING", "SUM", "TRAILING", "TREAT", "TRIM", "TYPE", "UPDATE", "UPPER", "VALUE", "WHEN", "WHERE"));

    private Keywords() {
    }

}
