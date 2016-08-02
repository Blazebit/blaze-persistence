package com.blazebit.persistence.criteria.impl.expression.function;

import javax.persistence.criteria.Expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AbsFunction<N> extends FunctionExpressionImpl<N> {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings({ "unchecked" })
    public AbsFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<N> expression) {
        super(criteriaBuilder, (Class<N>) expression.getJavaType(), "ABS", expression);
    }
}
