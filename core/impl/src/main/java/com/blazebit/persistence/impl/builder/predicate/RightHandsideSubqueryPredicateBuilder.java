/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.builder.predicate;

import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.SubqueryInternalBuilder;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.predicate.ExistsPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.predicate.PredicateBuilder;
import com.blazebit.persistence.parser.predicate.UnaryExpressionPredicate;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class RightHandsideSubqueryPredicateBuilder<T> extends SubqueryBuilderListenerImpl<T> implements PredicateBuilder {

    private final Predicate predicate;
    private final PredicateBuilderEndedListener listener;

    public RightHandsideSubqueryPredicateBuilder(PredicateBuilderEndedListener listener, Predicate predicate) {
        this.predicate = predicate;
        this.listener = listener;
    }

    @Override
    public void onBuilderEnded(SubqueryInternalBuilder<T> builder) {
        super.onBuilderEnded(builder);
        // set the finished subquery builder on the previously created predicate
        if (predicate instanceof ExistsPredicate && builder.getMaxResults() != Integer.MAX_VALUE) {
            // Since we render the limit in the subquery as wrapping function, there currently does not seem to be a possibility to support this in JPQL grammars
            throw new IllegalArgumentException("Limiting a subquery in an exists predicate is currently unsupported!");
        }

        if (predicate instanceof UnaryExpressionPredicate) {
            ((UnaryExpressionPredicate) predicate).setExpression(new SubqueryExpression(builder));
        } else {
            throw new IllegalStateException("SubqueryBuilder ended but predicate type was unexpected");
        }

        listener.onBuilderEnded(this);
    }

    @Override
    public Predicate getPredicate() {
        return predicate;
    }
}
