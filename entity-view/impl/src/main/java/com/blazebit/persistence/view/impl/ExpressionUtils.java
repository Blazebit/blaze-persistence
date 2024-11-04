/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.PathExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class ExpressionUtils {

    private ExpressionUtils() {
    }

    public static boolean isEmptyOrThis(Expression expression) {
        if (expression == null) {
            return true;
        }
        if (expression instanceof PathExpression) {
            PathExpression p = (PathExpression) expression;
            if (p.getExpressions().isEmpty() || p.getExpressions().size() == 1 && "this".equals(p.getExpressions().get(0).toString())) {
                return true;
            }
        }
        return false;
    }
}
