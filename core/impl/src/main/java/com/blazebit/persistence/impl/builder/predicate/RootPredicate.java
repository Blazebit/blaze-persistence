/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.builder.predicate;

import com.blazebit.persistence.impl.AbstractCommonQueryBuilder;
import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.modifier.ExpressionModifier;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.predicate.PredicateBuilder;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class RootPredicate extends PredicateBuilderEndedListenerImpl implements ExpressionModifier {

    private final CompoundPredicate predicate;
    private final ParameterManager parameterManager;
    private final ClauseType clauseType;
    private final AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder;

    public RootPredicate(ParameterManager parameterManager, ClauseType clauseType, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        this.predicate = new CompoundPredicate(CompoundPredicate.BooleanOperator.AND);
        this.parameterManager = parameterManager;
        this.clauseType = clauseType;
        this.queryBuilder = queryBuilder;
    }

    @Override
    public void onBuilderEnded(PredicateBuilder builder) {
        super.onBuilderEnded(builder);
        Predicate predicate = builder.getPredicate();

        // register parameter expressions
        parameterManager.collectParameterRegistrations(predicate, clauseType, queryBuilder);
        this.predicate.getChildren().add(predicate);
    }

    public CompoundPredicate getPredicate() {
        return predicate;
    }

    @Override
    public void set(Expression expression) {
        if (!(expression instanceof CompoundPredicate)) {
            throw new IllegalArgumentException("Expected compound predicate!");
        }
        if (expression != predicate) {
            predicate.getChildren().clear();
            predicate.getChildren().addAll(((CompoundPredicate) expression).getChildren());
        }
    }

    @Override
    public Expression get() {
        return predicate;
    }

    @Override
    public ExpressionModifier clone() {
        return this;
    }
}
