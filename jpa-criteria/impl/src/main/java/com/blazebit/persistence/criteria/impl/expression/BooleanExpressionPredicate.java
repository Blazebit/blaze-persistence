package com.blazebit.persistence.criteria.impl.expression;

import javax.persistence.criteria.Expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;

public class BooleanExpressionPredicate extends AbstractSimplePredicate {

    private static final long serialVersionUID = 1L;
    
    private final Expression<Boolean> expression;

    public BooleanExpressionPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated, Expression<Boolean> expression) {
        super(criteriaBuilder, negated);
        this.expression = expression;
    }

    @Override
    public AbstractPredicate copyNegated() {
        return new BooleanExpressionPredicate(criteriaBuilder, !isNegated(), expression);
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(expression);
    }

    @Override
    public void render(RenderContext context) {
        if (isNegated()) {
            context.getBuffer().append("NOT ");
        }
        context.apply(expression);
    }
    
}
