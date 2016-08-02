package com.blazebit.persistence.criteria.impl.expression;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder.Case;
import javax.persistence.criteria.Expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class GeneralCaseExpression<R> extends AbstractExpression<R> implements Case<R> {

    private static final long serialVersionUID = 1L;
    
    private Class<R> javaType;
    private Expression<? extends R> otherwiseResult;
    private final List<WhenClause> whenClauses = new ArrayList<WhenClause>();

    public class WhenClause {

        private final Expression<Boolean> condition;
        private final Expression<? extends R> result;

        public WhenClause(Expression<Boolean> condition, Expression<? extends R> result) {
            this.condition = condition;
            this.result = result;
        }

        public Expression<Boolean> getCondition() {
            return condition;
        }

        public Expression<? extends R> getResult() {
            return result;
        }
    }

    public GeneralCaseExpression(BlazeCriteriaBuilderImpl criteriaBuilder, Class<R> javaType) {
        super(criteriaBuilder, javaType);
        this.javaType = javaType;
    }

    @Override
    public Case<R> when(Expression<Boolean> condition, R result) {
        return when(condition, literal(result));
    }

    @SuppressWarnings({ "unchecked" })
    private LiteralExpression<R> literal(R result) {
        final Class<R> type = result != null ? (Class<R>) result.getClass() : getJavaType();
        return new LiteralExpression<R>(criteriaBuilder, type, result);
    }

    @Override
    public Case<R> when(Expression<Boolean> condition, Expression<? extends R> result) {
        WhenClause whenClause = new WhenClause(condition, result);
        whenClauses.add(whenClause);
        adjustJavaType(result);
        return this;
    }

    @SuppressWarnings({ "unchecked" })
    private void adjustJavaType(Expression<? extends R> exp) {
        if (javaType == null) {
            javaType = (Class<R>) exp.getJavaType();
        }
    }

    @Override
    public Expression<R> otherwise(R result) {
        return otherwise(literal(result));
    }

    @Override
    public Expression<R> otherwise(Expression<? extends R> result) {
        this.otherwiseResult = result;
        adjustJavaType(result);
        return this;
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        for (WhenClause whenClause : whenClauses) {
            visitor.visit(whenClause.getCondition());
            visitor.visit(whenClause.getResult());
        }
        
        visitor.visit(otherwiseResult);
    }

    @Override
    public void render(RenderContext context) {
        StringBuilder buffer = context.getBuffer();
        buffer.append("CASE");
        for (WhenClause whenClause : whenClauses) {
            buffer.append(" WHEN ");
            context.apply(whenClause.getCondition());
            buffer.append(" THEN ");
            context.apply(whenClause.getResult());
        }
        
        buffer.append(" ELSE ");
        context.apply(otherwiseResult);
        buffer.append(" END");
    }
    
}
