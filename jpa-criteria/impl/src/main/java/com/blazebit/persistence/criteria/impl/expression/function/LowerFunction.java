package com.blazebit.persistence.criteria.impl.expression.function;

import javax.persistence.criteria.Expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class LowerFunction extends FunctionExpressionImpl<String> {

    public static final String NAME = "LOWER";

    private static final long serialVersionUID = 1L;

    public LowerFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> string) {
        super(criteriaBuilder, String.class, NAME, string);
    }
}
