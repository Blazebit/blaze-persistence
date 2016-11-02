package com.blazebit.persistence.impl.util;

import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.FunctionExpression;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 15.09.2016.
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

    public static boolean isValueFunction(FunctionExpression e) {
        return "VALUE".equalsIgnoreCase(e.getFunctionName());
    }

    public static boolean isKeyFunction(FunctionExpression e) {
        return "KEY".equalsIgnoreCase(e.getFunctionName());
    }

    public static boolean isIndexFunction(FunctionExpression e) {
        return "INDEX".equalsIgnoreCase(e.getFunctionName());
    }

    public static boolean isEntryFunction(FunctionExpression e) {
        return "ENTRY".equalsIgnoreCase(e.getFunctionName());
    }

    public static boolean isCustomFunctionInvocation(FunctionExpression e) {
        return "FUNCTION".equalsIgnoreCase(e.getFunctionName());
    }

}
