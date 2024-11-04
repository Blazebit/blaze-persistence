/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.builder.predicate;

import com.blazebit.persistence.impl.SubqueryInternalBuilder;
import com.blazebit.persistence.impl.builder.expression.SuperExpressionSubqueryBuilderListener;
import com.blazebit.persistence.parser.expression.Expression;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class SuperExpressionLeftHandsideSubqueryPredicateBuilder<T extends LeftHandsideSubqueryPredicateBuilder> extends SuperExpressionSubqueryBuilderListener<T> {

    public SuperExpressionLeftHandsideSubqueryPredicateBuilder(String subqueryAlias, Expression superExpression) {
        super(subqueryAlias, superExpression);
    }

    @Override
    public void onBuilderEnded(SubqueryInternalBuilder<T> builder) {
        super.onBuilderEnded(builder);
        LeftHandsideSubqueryPredicateBuilder leftHandsideSubqueryPredicateBuilder = (LeftHandsideSubqueryPredicateBuilder) builder.getResult();
        leftHandsideSubqueryPredicateBuilder.setLeftExpression(superExpression);
    }
}
