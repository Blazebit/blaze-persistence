/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.builder.predicate;

import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListenerImpl;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class CaseExpressionBuilderListener extends ExpressionBuilderEndedListenerImpl {

    private final LeftHandsideSubqueryPredicateBuilder restrictionBuilder;

    public CaseExpressionBuilderListener(LeftHandsideSubqueryPredicateBuilder restrictionBuilder) {
        this.restrictionBuilder = restrictionBuilder;
    }

    @Override
    public void onBuilderEnded(ExpressionBuilder builder) {
        super.onBuilderEnded(builder);
        restrictionBuilder.setLeftExpression(builder.getExpression());
    }

}
