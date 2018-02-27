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
