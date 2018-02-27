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

import javax.persistence.criteria.CriteriaBuilder.SimpleCase;
import javax.persistence.criteria.Expression;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SimpleCaseExpression<C, R> extends AbstractExpression<R> implements SimpleCase<C, R> {

    private static final long serialVersionUID = 1L;

    private Class<R> javaType;
    private final Expression<? extends C> expression;
    private final List<WhenClause> whenClauses = new ArrayList<WhenClause>();
    private Expression<? extends R> otherwiseResult;

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public class WhenClause {

        private final LiteralExpression<C> condition;
        private final Expression<? extends R> result;

        public WhenClause(LiteralExpression<C> condition, Expression<? extends R> result) {
            this.condition = condition;
            this.result = result;
        }

    }

    public SimpleCaseExpression(BlazeCriteriaBuilderImpl criteriaBuilder, Class<R> javaType, Expression<? extends C> expression) {
        super(criteriaBuilder, javaType);
        this.javaType = javaType;
        this.expression = expression;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Expression<C> getExpression() {
        return (Expression<C>) expression;
    }

    @Override
    public Class<R> getJavaType() {
        return javaType;
    }

    @Override
    public SimpleCase<C, R> when(C condition, R result) {
        return when(condition, literal(result));
    }

    @SuppressWarnings({"unchecked"})
    private LiteralExpression<R> literal(R result) {
        final Class<R> type = result != null ? (Class<R>) result.getClass() : getJavaType();
        return new LiteralExpression<R>(criteriaBuilder, type, result);
    }

    @Override
    public SimpleCase<C, R> when(C condition, Expression<? extends R> result) {
        WhenClause whenClause = new WhenClause(new LiteralExpression<C>(criteriaBuilder, condition), result);
        whenClauses.add(whenClause);
        adjustJavaType(result);
        return this;
    }

    @SuppressWarnings({"unchecked"})
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
        visitor.visit(expression);

        for (WhenClause whenClause : whenClauses) {
            visitor.visit(whenClause.result);
        }

        visitor.visit(otherwiseResult);
    }

    @Override
    public void render(RenderContext context) {
        StringBuilder buffer = context.getBuffer();
        buffer.append("CASE ");
        context.apply(expression);
        for (WhenClause whenClause : whenClauses) {
            buffer.append(" WHEN ");
            context.apply(whenClause.condition);
            buffer.append(" THEN ");
            context.apply(whenClause.result);
        }

        buffer.append(" ELSE ");
        context.apply(otherwiseResult);
        buffer.append(" END");
    }

}
