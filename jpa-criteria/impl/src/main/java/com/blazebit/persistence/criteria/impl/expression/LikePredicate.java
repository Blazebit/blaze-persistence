/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;

import javax.persistence.criteria.Expression;

/**
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

    public LikePredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated, Expression<String> matchExpression, Expression<String> pattern, Expression<Character> escapeCharacter) {
        super(criteriaBuilder, negated);
        this.matchExpression = matchExpression;
        this.pattern = pattern;
        this.escapeCharacter = escapeCharacter;
    }

    @Override
    public AbstractPredicate copyNegated() {
        return new LikePredicate(criteriaBuilder, !isNegated(), matchExpression, pattern, escapeCharacter);
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        if (escapeCharacter != null) {
            visitor.visit(escapeCharacter);
        }
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
