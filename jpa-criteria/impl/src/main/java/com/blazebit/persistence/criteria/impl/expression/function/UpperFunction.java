package com.blazebit.persistence.criteria.impl.expression.function;

import javax.persistence.criteria.Expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class UpperFunction extends FunctionExpressionImpl<String> {

    private static final long serialVersionUID = 1L;
    
    public static final String NAME = "UPPER";

    public UpperFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> string) {
        super(criteriaBuilder, String.class, NAME, string);
    }
}
