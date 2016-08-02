package com.blazebit.persistence.criteria.impl.expression.function;

import java.util.List;

import javax.persistence.criteria.Expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class FunctionFunction<X> extends FunctionExpressionImpl<X> {

    private static final long serialVersionUID = 1L;
    
    private final String name;
    
    public FunctionFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Class<X> javaType, String functionName, Expression<?>... argumentExpressions) {
        super(criteriaBuilder, javaType, "FUNCTION", argumentExpressions);
        this.name = functionName;
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        List<Expression<?>> args = getArgumentExpressions();
        buffer.append("FUNCTION('").append(name).append('\'');
        for (int i = 0; i < args.size(); i++) {
            buffer.append(',');
            context.apply(args.get(i));
        }
        buffer.append(')');
    }
}
