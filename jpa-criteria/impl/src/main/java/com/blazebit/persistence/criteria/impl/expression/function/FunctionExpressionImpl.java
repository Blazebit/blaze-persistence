package com.blazebit.persistence.criteria.impl.expression.function;

import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.Expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class FunctionExpressionImpl<X> extends AbstractFunctionExpression<X> {

    private static final long serialVersionUID = 1L;
    
    private final List<Expression<?>> argumentExpressions;

    public FunctionExpressionImpl(BlazeCriteriaBuilderImpl criteriaBuilder, Class<X> javaType, String functionName, Expression<?>... argumentExpressions) {
        super(criteriaBuilder, javaType, functionName);
        this.argumentExpressions = Arrays.asList(argumentExpressions);
    }

    public List<Expression<?>> getArgumentExpressions() {
        return argumentExpressions;
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        for (Expression<?> argument : getArgumentExpressions()) {
            visitor.visit(argument);
        }
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        List<Expression<?>> args = getArgumentExpressions();
        buffer.append(getFunctionName()).append('(');
        for (int i = 0; i < args.size(); i++) {
            if (i != 0) {
                buffer.append(',');
            }
            
            context.apply(args.get(i));
        }
        buffer.append(')');
    }
}
