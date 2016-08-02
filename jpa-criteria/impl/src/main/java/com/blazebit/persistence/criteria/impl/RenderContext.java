package com.blazebit.persistence.criteria.impl;

import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Selection;

import com.blazebit.persistence.SubqueryInitiator;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface RenderContext {

    public StringBuilder getBuffer();

    public SubqueryInitiator<?> getSubqueryInitiator();

    public ClauseType getClauseType();

    public void apply(Selection<?> selection);

    public static enum ClauseType {
        SET,
        SELECT,
        FROM,
        ON,
        WHERE,
        GROUP_BY,
        HAVING,
        ORDER_BY
    }

    public String generateAlias(Class<?> entityClass);

    public String generateSubqueryAlias(InternalQuery<?> query);

    public String registerExplicitParameter(ParameterExpression<?> criteriaQueryParameter);

    public String registerLiteralParameterBinding(Object literal, Class javaType);
}
