package com.blazebit.persistence.criteria.impl.expression.function;

import java.util.Collection;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.AbstractExpression;
import com.blazebit.persistence.criteria.impl.path.PluralAttributePath;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SizeFunction<C extends Collection> extends AbstractExpression<Integer> {

    private static final long serialVersionUID = 1L;
    
    private final PluralAttributePath<C> collectionPath;

    public SizeFunction(BlazeCriteriaBuilderImpl criteriaBuilder, PluralAttributePath<C> collectionPath) {
        super(criteriaBuilder, Integer.class);
        this.collectionPath = collectionPath;
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        // no-op
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        buffer.append("SIZE(");
        context.apply(collectionPath);
        buffer.append(')');
    }
    
}
