/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.util;

import com.blazebit.persistence.parser.expression.ArrayExpression;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.expression.QualifiedExpression;
import com.blazebit.persistence.parser.expression.TreatExpression;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class ExpressionUtils {

    private ExpressionUtils() {
    }

    public static boolean isSizeFunction(Expression expression) {
        if (expression instanceof FunctionExpression) {
            return isSizeFunction((FunctionExpression) expression);
        }
        return false;
    }

    public static boolean isSizeFunction(FunctionExpression expression) {
        return "SIZE".equalsIgnoreCase(expression.getFunctionName());
    }

    public static boolean isOuterFunction(FunctionExpression e) {
        return "OUTER".equalsIgnoreCase(e.getFunctionName());
    }

    public static boolean isCustomFunctionInvocation(FunctionExpression e) {
        return "FUNCTION".equalsIgnoreCase(e.getFunctionName());
    }

    public static boolean isCountFunction(Expression expression) {
        if (expression instanceof FunctionExpression) {
            return isCountFunction((FunctionExpression) expression);
        }
        return false;
    }

    public static boolean isCountFunction(FunctionExpression expr) {
        return "COUNT".equalsIgnoreCase(expr.getFunctionName());
    }

    public static PathExpression getLeftMostPathExpression(PathExpression leftMost) {
        PathElementExpression pathElementExpression;
        while (!((pathElementExpression = leftMost.getExpressions().get(0)) instanceof PropertyExpression)) {
            if (pathElementExpression instanceof TreatExpression) {
                Expression treatPath = ((TreatExpression) pathElementExpression).getExpression();
                if (treatPath instanceof QualifiedExpression) {
                    leftMost = ((QualifiedExpression) treatPath).getPath();
                } else {
                    leftMost = (PathExpression) treatPath;
                }
            } else if (pathElementExpression instanceof QualifiedExpression) {
                leftMost = ((QualifiedExpression) pathElementExpression).getPath();
            } else if (pathElementExpression instanceof ArrayExpression) {
                break;
            } else {
                throw new IllegalArgumentException("Unsupported expression: " + pathElementExpression);
            }
        }
        return leftMost;
    }
}
