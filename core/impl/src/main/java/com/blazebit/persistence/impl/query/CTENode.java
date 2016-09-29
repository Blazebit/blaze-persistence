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
    private final boolean unionAll;
    private final QuerySpecification nonRecursiveQuerySpecification;
    private final QuerySpecification recursiveQuerySpecification;
    private final Map<String, String> nonRecursiveTableNameRemappings;
    private final Map<String, String> recursiveTableNameRemappings;
    private final String nonRecursiveWithClauseSuffix;

    public CTENode(String name, String entityName, String head, boolean unionAll, QuerySpecification nonRecursiveQuerySpecification, QuerySpecification recursiveQuerySpecification, Map<String, String> nonRecursiveTableNameRemappings, Map<String, String> recursiveTableNameRemappings, String nonRecursiveWithClauseSuffix) {
        this.name = name;
        this.entityName = entityName;
        this.head = head;
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
