package com.blazebit.persistence.criteria.impl.expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class NullLiteralExpression<T> extends AbstractExpression<T> {

    private static final long serialVersionUID = 1L;

    public NullLiteralExpression(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> type) {
        super( criteriaBuilder, type );
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        // no-op
    }

    @Override
    public void render(RenderContext context) {
        context.getBuffer().append("NULL");
    }

}
