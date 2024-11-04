/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.ParameterExpression;
import com.blazebit.persistence.parser.expression.VisitorAdapter;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class ParameterUnregistrationVisitor extends VisitorAdapter {

    private final ParameterManager parameterManager;
    private ClauseType clauseType;
    private AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder;

    public ParameterUnregistrationVisitor(ParameterManager parameterManager) {
        this.parameterManager = parameterManager;
    }

    @Override
    public void visit(ParameterExpression expression) {
        parameterManager.unregisterParameterName(expression.getName(), clauseType, queryBuilder);
    }

    public ClauseType getClauseType() {
        return clauseType;
    }

    public void setClauseType(ClauseType clauseType) {
        this.clauseType = clauseType;
    }

    public AbstractCommonQueryBuilder<?, ?, ?, ?, ?> getQueryBuilder() {
        return queryBuilder;
    }

    public void setQueryBuilder(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        this.queryBuilder = queryBuilder;
    }
}
