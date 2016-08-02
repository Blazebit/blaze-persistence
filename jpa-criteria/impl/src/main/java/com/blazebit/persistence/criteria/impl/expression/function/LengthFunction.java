package com.blazebit.persistence.criteria.impl.expression.function;

import javax.persistence.criteria.Expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class LengthFunction extends FunctionExpressionImpl<Integer> {

    private static final long serialVersionUID = 1L;
    
    public static final String NAME = "LENGTH";

    public LengthFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> value) {
        super(criteriaBuilder, Integer.class, NAME, value);
    }
}
