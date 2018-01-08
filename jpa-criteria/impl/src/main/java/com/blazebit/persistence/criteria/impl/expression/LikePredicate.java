/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
