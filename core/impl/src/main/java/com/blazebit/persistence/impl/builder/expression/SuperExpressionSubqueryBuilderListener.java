/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.builder.expression;

import com.blazebit.persistence.impl.ExpressionUtils;
import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.SubqueryInternalBuilder;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.SubqueryExpression;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class SuperExpressionSubqueryBuilderListener<T> extends SubqueryBuilderListenerImpl<T> {

    protected Expression superExpression;
    private final String subqueryAlias;

    public SuperExpressionSubqueryBuilderListener(String subqueryAlias, Expression superExpression) {
        this.subqueryAlias = subqueryAlias;
        this.superExpression = superExpression;
    }

    @Override
    public void onBuilderEnded(SubqueryInternalBuilder<T> builder) {
        super.onBuilderEnded(builder);
        superExpression = ExpressionUtils.replaceSubexpression(superExpression, subqueryAlias, new SubqueryExpression(builder));
    }

}
