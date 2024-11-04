/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.SubqueryInitiator;

import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Selection;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface RenderContext {

    public StringBuilder getBuffer();

    public SubqueryInitiator<?> getSubqueryInitiator();

    public ClauseType getClauseType();

    public void apply(Selection<?> selection);

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
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

    public String resolveAlias(Object aliasedObject, Class<?> entityClass);

    public String resolveAlias(Object aliasedObject, String name);

    public String generateSubqueryAlias(InternalQuery<?> query);

    public String registerExplicitParameter(ParameterExpression<?> criteriaQueryParameter);

    public String registerLiteralParameterBinding(Object literal, Class javaType);
}
