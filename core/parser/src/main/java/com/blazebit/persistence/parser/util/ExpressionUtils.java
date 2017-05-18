/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.parser.util;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.FunctionExpression;

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
}
