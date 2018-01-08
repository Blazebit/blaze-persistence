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

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CTENode {

    private final String name;
    private final String entityName;
    private final String head;
    private final String[] aliases;
    private final boolean unionAll;
    private final QuerySpecification nonRecursiveQuerySpecification;
    private final QuerySpecification recursiveQuerySpecification;
    private final Map<String, String> nonRecursiveTableNameRemappings;
    private final Map<String, String> recursiveTableNameRemappings;
    private final String nonRecursiveWithClauseSuffix;

    public CTENode(String name, String entityName, String head, String[] aliases, boolean unionAll, QuerySpecification nonRecursiveQuerySpecification, QuerySpecification recursiveQuerySpecification, Map<String, String> nonRecursiveTableNameRemappings, Map<String, String> recursiveTableNameRemappings, String nonRecursiveWithClauseSuffix) {
        this.name = name;
        this.entityName = entityName;
        this.head = head;
        this.aliases = aliases;
        this.unionAll = unionAll;
        this.nonRecursiveQuerySpecification = nonRecursiveQuerySpecification;
        this.recursiveQuerySpecification = recursiveQuerySpecification;
        this.nonRecursiveTableNameRemappings = nonRecursiveTableNameRemappings;
        this.recursiveTableNameRemappings = recursiveTableNameRemappings;
        this.nonRecursiveWithClauseSuffix = nonRecursiveWithClauseSuffix;
    }

    public String getName() {
        return name;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getHead() {
        return head;
    }

    public String[] getAliases() {
        return aliases;
    }

    public boolean isUnionAll() {
        return unionAll;
    }

    public QuerySpecification getNonRecursiveQuerySpecification() {
        return nonRecursiveQuerySpecification;
    }

    public QuerySpecification getRecursiveQuerySpecification() {
        return recursiveQuerySpecification;
    }

    public boolean isRecursive() {
        return recursiveQuerySpecification != null;
    }

    public Map<String, String> getNonRecursiveTableNameRemappings() {
        return nonRecursiveTableNameRemappings;
    }

    public Map<String, String> getRecursiveTableNameRemappings() {
        return recursiveTableNameRemappings;
    }

    public String getNonRecursiveWithClauseSuffix() {
        return nonRecursiveWithClauseSuffix;
    }
}
