package com.blazebit.persistence.impl.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Moritz Becker
 * @since 1.2
 */
public class TypeFunctionExpression extends FunctionExpression {

    public TypeFunctionExpression(Expression expression) {
        super("TYPE", new ArrayList<Expression>(Arrays.asList(expression)));
    }
}
