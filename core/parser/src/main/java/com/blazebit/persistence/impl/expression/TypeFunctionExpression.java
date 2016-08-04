package com.blazebit.persistence.impl.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 04.08.2016.
 */
public class TypeFunctionExpression extends FunctionExpression {

    public TypeFunctionExpression(Expression expression) {
        super("TYPE", new ArrayList<Expression>(Arrays.asList(expression)));
    }
}
