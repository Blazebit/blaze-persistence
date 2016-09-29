package com.blazebit.persistence.impl.query;

import javax.persistence.Query;

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
    private final Query valueQuery;

    public EntityFunctionNode(String valuesClause, String valuesAliases, Class<?> entityClass, String tableAlias, Query valueQuery) {
        this.valuesClause = valuesClause;
        this.valuesAliases = valuesAliases;
        this.entityClass = entityClass;
        this.tableAlias = tableAlias;
        this.valueQuery = valueQuery;
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

    public Query getValueQuery() {
        return valueQuery;
    }
}
