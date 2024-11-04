/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.base.jpa.assertion;

import com.blazebit.persistence.testsuite.base.jpa.RelationalModelAccessor;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AssertSelectStatementBuilder extends AbstractAssertStatementBuilder<AssertSelectStatementBuilder> {

    private final List<String> fetchedTables = new ArrayList<>();

    public AssertSelectStatementBuilder(AssertStatementBuilder parentBuilder, RelationalModelAccessor relationalModelAccessor) {
        super(parentBuilder, relationalModelAccessor);
    }

    public AssertSelectStatementBuilder fetching(Class<?> entityClass, String relationName) {
        String table = tableFromEntityRelation(entityClass, relationName);
        if (table != null) {
            tables.add(table);
            fetchedTables.add(table);
        }

        return this;
    }

    public AssertSelectStatementBuilder fetching(Class<?>... entityClasses) {
        for (Class<?> entityClass : entityClasses) {
            String table = tableFromEntity(entityClass);
            if (table != null) {
                tables.add(table);
                fetchedTables.add(table);
            }
        }

        return this;
    }

    @Override
    protected AssertStatement build() {
        return new AssertSelectStatement(tables, fetchedTables);
    }
}
