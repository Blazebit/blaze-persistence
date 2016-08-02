package com.blazebit.persistence.criteria.impl.expression.function;

import javax.persistence.criteria.Expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.LiteralExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SubstringFunction extends AbstractFunctionExpression<String> {

    private static final long serialVersionUID = 1L;

    public static final String NAME = "SUBSTRING";

    private final Expression<String> value;
    private final Expression<Integer> start;
    private final Expression<Integer> length;

    public SubstringFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> value, Expression<Integer> start, Expression<Integer> length) {
        super(criteriaBuilder, String.class, NAME);
        this.value = value;
        this.start = start;
        this.length = length;
    }

    public SubstringFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> value, Expression<Integer> start) {
        this(criteriaBuilder, value, start, (Expression<Integer>) null);
    }

    public SubstringFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> value, int start) {
        this(criteriaBuilder, value, new LiteralExpression<Integer>(criteriaBuilder, start));
    }

    public SubstringFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> value, int start, int length) {
        this(criteriaBuilder, value, new LiteralExpression<Integer>(criteriaBuilder, start), new LiteralExpression<Integer>(criteriaBuilder, length));
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(value);
        visitor.visit(start);
        visitor.visit(length);
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        buffer.append("SUBSTRING(");
        context.apply(value);
        buffer.append(',');
        context.apply(start);
        
        if (length != null) {
            buffer.append(',');
            context.apply(length);
        }
        
        buffer.append(')');
    }
    
}
