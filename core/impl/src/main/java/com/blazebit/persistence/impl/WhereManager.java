/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.WhereBuilder;
import com.blazebit.persistence.WhereOrBuilder;
import com.blazebit.persistence.impl.builder.predicate.WhereOrBuilderImpl;
import com.blazebit.persistence.parser.expression.ExpressionFactory;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class WhereManager<T extends WhereBuilder<T>> extends PredicateManager<T> {

    WhereManager(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        super(queryGenerator, parameterManager, subqueryInitFactory, expressionFactory);
    }

    @Override
    protected String getClauseName() {
        return "WHERE";
    }

    @Override
    public ClauseType getClauseType() {
        return ClauseType.WHERE;
    }

    @SuppressWarnings("unchecked")
    WhereOrBuilder<T> whereOr(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder) {
        return rootPredicate.startBuilder(new WhereOrBuilderImpl<T>((T) builder, rootPredicate, subqueryInitFactory, expressionFactory, parameterManager));
    }
}
