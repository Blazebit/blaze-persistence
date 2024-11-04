/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.builder.predicate;

import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.SubqueryInternalBuilder;
import com.blazebit.persistence.parser.expression.SubqueryExpression;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class LeftHandsideSubqueryPredicateBuilderListener<T extends LeftHandsideSubqueryPredicateBuilder> extends SubqueryBuilderListenerImpl<T> {

    @Override
    public void onBuilderEnded(SubqueryInternalBuilder<T> builder) {
        super.onBuilderEnded(builder);
        T leftHandsideSubqueryPredicateBuilder = builder.getResult();
        leftHandsideSubqueryPredicateBuilder.setLeftExpression(new SubqueryExpression(builder));
    }
}
