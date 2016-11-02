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
public class LikePredicate extends AbstractSimplePredicate {

    private static final long serialVersionUID = 1L;

    private final Expression<String> matchExpression;
    private final Expression<String> pattern;
    private final Expression<Character> escapeCharacter;

    public LikePredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated, Expression<String> matchExpression, Expression<String> pattern) {
        this(criteriaBuilder, negated, matchExpression, pattern, null);
    }

    public LikePredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated, Expression<String> matchExpression, String pattern) {
        this(criteriaBuilder, negated, matchExpression, new LiteralExpression<String>(criteriaBuilder, pattern));
    }

    public LikePredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated, Expression<String> matchExpression, Expression<String> pattern, Expression<Character> escapeCharacter) {
        super(criteriaBuilder, negated);
        this.matchExpression = matchExpression;
        this.pattern = pattern;
        this.escapeCharacter = escapeCharacter;
    }

    public LikePredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated, Expression<String> matchExpression, Expression<String> pattern, char escapeCharacter) {
        this(criteriaBuilder, negated, matchExpression, pattern, new LiteralExpression<Character>(criteriaBuilder, escapeCharacter));
    }

    public LikePredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated, Expression<String> matchExpression, String pattern, char escapeCharacter) {
        this(criteriaBuilder, negated, matchExpression, new LiteralExpression<String>(criteriaBuilder, pattern), new LiteralExpression<Character>(criteriaBuilder, escapeCharacter));
    }

    public LikePredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated, Expression<String> matchExpression, String pattern, Expression<Character> escapeCharacter) {
        this(criteriaBuilder, negated, matchExpression, new LiteralExpression<String>(criteriaBuilder, pattern), escapeCharacter);
    }

    @Override
    public AbstractPredicate copyNegated() {
        return new LikePredicate(criteriaBuilder, !isNegated(), matchExpression, pattern, escapeCharacter);
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(escapeCharacter);
        visitor.visit(matchExpression);
        visitor.visit(pattern);
    }

    @Override
    public void render(RenderContext context) {
        StringBuilder buffer = context.getBuffer();
        context.apply(matchExpression);
        
        if (isNegated()) {
            buffer.append(" NOT");
        }

        buffer.append(" LIKE ");
        context.apply(pattern);
        
        Expression<Character> escapeExpr = escapeCharacter;
        if (escapeExpr != null) {
            buffer.append(" ESCAPE ");
            context.apply(escapeExpr);
        }
    }
    
}
