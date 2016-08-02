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
public class LocateFunction extends AbstractFunctionExpression<Integer> {

    private static final long serialVersionUID = 1L;

    public static final String NAME = "LOCATE";

    private final Expression<String> pattern;
    private final Expression<String> string;
    private final Expression<Integer> start;

    public LocateFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> pattern, Expression<String> string, Expression<Integer> start) {
        super(criteriaBuilder, Integer.class, NAME);
        this.pattern = pattern;
        this.string = string;
        this.start = start;
    }

    public LocateFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> pattern, Expression<String> string) {
        this(criteriaBuilder, pattern, string, null);
    }

    public LocateFunction(BlazeCriteriaBuilderImpl criteriaBuilder, String pattern, Expression<String> string) {
        this(criteriaBuilder, new LiteralExpression<String>(criteriaBuilder, pattern), string, null);
    }

    public LocateFunction(BlazeCriteriaBuilderImpl criteriaBuilder, String pattern, Expression<String> string, int start) {
        this(criteriaBuilder, new LiteralExpression<String>(criteriaBuilder, pattern), string, new LiteralExpression<Integer>(criteriaBuilder, start));
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(pattern);
        visitor.visit(start);
        visitor.visit(string);
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        buffer.append("LOCATE(");
        context.apply(pattern);
        buffer.append(',');
        context.apply(string);
        
        if (start != null) {
            buffer.append(',');
            context.apply(start);
        }
        
        buffer.append(')');
    }
    
}
