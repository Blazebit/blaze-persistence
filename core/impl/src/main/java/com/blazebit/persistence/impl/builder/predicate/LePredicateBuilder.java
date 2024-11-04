/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.builder.predicate;

import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.predicate.PredicateQuantifier;
import com.blazebit.persistence.parser.predicate.LePredicate;
import com.blazebit.persistence.parser.predicate.QuantifiableBinaryExpressionPredicate;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class LePredicateBuilder<T> extends AbstractQuantifiablePredicateBuilder<T> {

    public LePredicateBuilder(T result, PredicateBuilderEndedListener listener, Expression leftExpression, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, ParameterManager parameterManager, ClauseType clauseType) {
        super(result, listener, leftExpression, false, subqueryInitFactory, expressionFactory, parameterManager, clauseType);
    }

    @Override
    protected QuantifiableBinaryExpressionPredicate createPredicate(Expression left, Expression right, PredicateQuantifier quantifier) {
        return new LePredicate(left, right, quantifier, false);
    }

}
