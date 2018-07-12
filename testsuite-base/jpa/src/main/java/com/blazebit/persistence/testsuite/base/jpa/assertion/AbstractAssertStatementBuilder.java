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
public abstract class AbstractAssertStatementBuilder<T extends AbstractAssertStatementBuilder<T>> {

    protected final List<String> tables = new ArrayList<>();
    private final AssertStatementBuilder parentBuilder;
    private final RelationalModelAccessor relationalModelAccessor;

    public AbstractAssertStatementBuilder(AssertStatementBuilder parentBuilder, RelationalModelAccessor relationalModelAccessor) {
        this.parentBuilder = parentBuilder;
        this.relationalModelAccessor = relationalModelAccessor;
        parentBuilder.setCurrentBuilder(this);
    }

    protected abstract AssertStatement build();

    @SuppressWarnings("unchecked")
    public T forEntity(Class<?> entityClass) {
        String table = tableFromEntity(entityClass);
        if (table != null) {
            tables.add(table);
        }
        return (T) this;
    }

    public T forRelation(Class<?> entityClass, String relationName) {
        String table = tableFromEntityRelation(entityClass, relationName);
        if (table != null) {
            tables.add(table);
        }

        return (T) this;
    }

    public AssertStatementBuilder and() {
        parentBuilder.unsetCurrentBuilder(this);
        parentBuilder.addStatement(build());
        return parentBuilder;
    }

    public void validate() {
        and().validate();
    }

    protected String tableFromEntity(Class<?> entityClass) {
        if (relationalModelAccessor == null) {
            return null;
        }

        return relationalModelAccessor.tableFromEntity(entityClass).toLowerCase();
    }

    protected String tableFromEntityRelation(Class<?> entityClass, String relationName) {
        if (relationalModelAccessor == null) {
            return null;
        }

        return relationalModelAccessor.tableFromEntityRelation(entityClass, relationName).toLowerCase();
    }
}
