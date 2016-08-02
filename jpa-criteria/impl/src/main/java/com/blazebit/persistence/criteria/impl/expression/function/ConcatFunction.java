package com.blazebit.persistence.criteria.impl.expression.function;

import javax.persistence.criteria.Expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.AbstractExpression;
import com.blazebit.persistence.criteria.impl.expression.LiteralExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ConcatFunction extends AbstractExpression<String> {

    private static final long serialVersionUID = 1L;
    
    private final Expression<String> string1;
    private final Expression<String> string2;

    public ConcatFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> expression1, Expression<String> expression2) {
        super(criteriaBuilder, String.class);
        this.string1 = expression1;
        this.string2 = expression2;
    }

    public ConcatFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> string1, String string2) {
        this(criteriaBuilder, string1, wrap(criteriaBuilder, string2));
    }

    private static Expression<String> wrap(BlazeCriteriaBuilderImpl criteriaBuilder, String string) {
        return new LiteralExpression<String>(criteriaBuilder, string);
    }

    public ConcatFunction(BlazeCriteriaBuilderImpl criteriaBuilder, String string1, Expression<String> string2) {
        this(criteriaBuilder, wrap(criteriaBuilder, string1), string2);
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(string1);
        visitor.visit(string2);
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        buffer.append("CONCAT(");
        context.apply(string1);
        buffer.append(',');
        context.apply(string2);
        buffer.append(')');
    }
    
}
