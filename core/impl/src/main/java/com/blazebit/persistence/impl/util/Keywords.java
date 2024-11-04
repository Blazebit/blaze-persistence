/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
