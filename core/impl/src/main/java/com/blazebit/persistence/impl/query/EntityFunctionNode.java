/*
 * Copyright 2014 - 2022 Blazebit.
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

    private final String subquery;
    private final String aliases;
    private final String entityName;
    private final String tableAlias;
    private final String pluralCollectionTableAlias;
    private final String pluralTableAlias;
    private final String pluralTableJoin;
    private final String syntheticPredicate;
    private final boolean lateral;

    public EntityFunctionNode(String subquery, String aliases, String entityName, String tableAlias, String pluralCollectionTableAlias, String pluralTableAlias, String pluralTableJoin, String syntheticPredicate, boolean lateral) {
        this.subquery = subquery;
        this.aliases = aliases;
        this.entityName = entityName;
        this.tableAlias = tableAlias;
        this.pluralCollectionTableAlias = pluralCollectionTableAlias;
        this.pluralTableAlias = pluralTableAlias;
        this.pluralTableJoin = pluralTableJoin;
        this.syntheticPredicate = syntheticPredicate;
        this.lateral = lateral;
    }

    public String getSubquery() {
        return subquery;
    }

    public String getAliases() {
        return aliases;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public String getPluralCollectionTableAlias() {
        return pluralCollectionTableAlias;
    }

    public String getPluralTableAlias() {
        return pluralTableAlias;
    }

    public String getPluralTableJoin() {
        return pluralTableJoin;
    }

    public String getSyntheticPredicate() {
        return syntheticPredicate;
    }

    public boolean isLateral() {
        return lateral;
    }
}
