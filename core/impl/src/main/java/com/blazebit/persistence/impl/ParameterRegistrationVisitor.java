/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.ArrayExpression;
import com.blazebit.persistence.parser.expression.ParameterExpression;
import com.blazebit.persistence.parser.expression.VisitorAdapter;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class ParameterRegistrationVisitor extends VisitorAdapter {

    private final ParameterManager parameterManager;
    private ClauseType clauseType;
    private ClauseType secondClauseType;
    private AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder;

    public ParameterRegistrationVisitor(ParameterManager parameterManager) {
        this.parameterManager = parameterManager;
    }

    @Override
    public void visit(ParameterExpression expression) {
        // Value was not set so we only have an unsatisfied parameter name which we register
        if (AbstractFullQueryBuilder.ID_PARAM_NAME.equals(expression.getName())) {
            throw new IllegalArgumentException("The parameter name '" + expression.getName() + "' is reserved - use a different name");
        } else {
            parameterManager.registerParameterName(expression.getName(), expression.isCollectionValued(), clauseType, queryBuilder);
            if (secondClauseType != null) {
                parameterManager.registerParameterName(expression.getName(), expression.isCollectionValued(), secondClauseType, queryBuilder);
            }
            if (expression.getValue() != null) {
                parameterManager.satisfyParameter(expression.getName(), expression.getValue());
            }
        }
    }

    @Override
    public void visit(ArrayExpression expression) {
        expression.getBase().accept(this);
        // The index will always end up in the on clause
        ClauseType oldClauseType = secondClauseType;
        secondClauseType = ClauseType.JOIN;
        expression.getIndex().accept(this);
        secondClauseType = oldClauseType;
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
