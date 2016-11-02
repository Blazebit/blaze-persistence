package com.blazebit.persistence.criteria.impl.expression;

import javax.persistence.criteria.Expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class IsNullPredicate extends AbstractSimplePredicate {

    private static final long serialVersionUID = 1L;
    
    private final Expression<?> operand;

    public IsNullPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated, Expression<?> operand) {
        super(criteriaBuilder, negated);
        this.operand = operand;
    }

    @Override
    public AbstractPredicate copyNegated() {
        return new IsNullPredicate(criteriaBuilder, !isNegated(), operand);
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(operand);
    }

    @Override
    public void render(RenderContext context) {
        context.apply(operand);
        
        if (isNegated()) {
            context.getBuffer().append(" IS NOT NULL");
        } else {
            context.getBuffer().append(" IS NULL");
        }
    }

}
