/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class TypeFunctionExpression extends FunctionExpression {

    public TypeFunctionExpression(Expression expression) {
        super("TYPE", new ArrayList<Expression>(Arrays.asList(expression)));
    }

    @Override
    public TypeFunctionExpression copy(ExpressionCopyContext copyContext) {
        return new TypeFunctionExpression(expressions.get(0).copy(copyContext));
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
