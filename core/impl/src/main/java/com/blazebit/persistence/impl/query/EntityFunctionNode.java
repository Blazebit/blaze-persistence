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

package com.blazebit.persistence.impl.query;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityFunctionNode {

    private final String valuesClause;
    private final String valuesAliases;
    private final Class<?> entityClass;
    private final String tableAlias;
    private final String syntheticPredicate;

    public EntityFunctionNode(String valuesClause, String valuesAliases, Class<?> entityClass, String tableAlias, String syntheticPredicate) {
        this.valuesClause = valuesClause;
        this.valuesAliases = valuesAliases;
        this.entityClass = entityClass;
        this.tableAlias = tableAlias;
        this.syntheticPredicate = syntheticPredicate;
    }

    public String getValuesClause() {
        return valuesClause;
    }

    public String getValuesAliases() {
        return valuesAliases;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public String getSyntheticPredicate() {
        return syntheticPredicate;
    }
}
