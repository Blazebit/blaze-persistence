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

package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.UpdateCriteriaBuilder;
import com.blazebit.persistence.criteria.BlazeCriteriaUpdate;
import com.blazebit.persistence.criteria.impl.expression.AbstractExpression;
import com.blazebit.persistence.criteria.impl.expression.LiteralExpression;
import com.blazebit.persistence.criteria.impl.path.AbstractPath;
import com.blazebit.persistence.criteria.impl.path.SingularAttributePath;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BlazeCriteriaUpdateImpl<T> extends AbstractModificationCriteriaQuery<T> implements BlazeCriteriaUpdate<T> {

    private final List<Assignment> assignments = new ArrayList<Assignment>();

    public BlazeCriteriaUpdateImpl(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> targetEntity, String alias) {
        super(criteriaBuilder);
        from(targetEntity, alias);
    }

    @Override
    public <Y, X extends Y> BlazeCriteriaUpdate<T> set(SingularAttribute<? super T, Y> attribute, X value) {
        Path<?> attributePath = getRoot().get(attribute);
        return internalSet(attributePath, valueExpression(attributePath, value));
    }

    @Override
    public <Y> BlazeCriteriaUpdate<T> set(SingularAttribute<? super T, Y> attribute, Expression<? extends Y> value) {
        Path<?> attributePath = getRoot().get(attribute);
        return internalSet(attributePath, value);
    }

    @Override
    public <Y, X extends Y> BlazeCriteriaUpdate<T> set(Path<Y> attribute, X value) {
        return internalSet(attribute, valueExpression(attribute, value));
    }

    @Override
    public <Y> BlazeCriteriaUpdate<T> set(Path<Y> attribute, Expression<? extends Y> value) {
        return internalSet(attribute, value);
    }

    @Override
    public BlazeCriteriaUpdate<T> set(String attributeName, Object value) {
        final Path<?> attributePath = getRoot().get(attributeName);
        return internalSet(attributePath, valueExpression(attributePath, value));
    }

    private AbstractExpression<?> valueExpression(Path<?> attributePath, Object value) {
        if (value == null) {
            return criteriaBuilder.nullLiteral(attributePath.getJavaType());
        } else if (value instanceof AbstractExpression<?>) {
            return (AbstractExpression<?>) value;
        } else {
            return criteriaBuilder.literal(value);
        }
    }

    private BlazeCriteriaUpdate<T> internalSet(Path<?> attribute, Expression<?> value) {
        if (!(attribute instanceof AbstractPath<?>)) {
            throw new IllegalArgumentException("Illegal custom attribute path: " + attribute.getClass().getName());
        }
        if (!(attribute instanceof SingularAttributePath<?>)) {
            throw new IllegalArgumentException("Only singular attributes can be updated");
        }
        if (value == null) {
            throw new IllegalArgumentException("Illegal null expression passed. Check your set-call, you probably wanted to pass a literal null");
        }
        if (!(value instanceof AbstractExpression<?>)) {
            throw new IllegalArgumentException("Illegal custom value expression: " + value.getClass().getName());
        }
        assignments.add(new Assignment((SingularAttributePath<?>) attribute, (AbstractExpression<?>) value));
        return this;
    }

    @Override
    public BlazeCriteriaUpdate<T> where(Expression<Boolean> restriction) {
        setRestriction(restriction);
        return this;
    }

    @Override
    public BlazeCriteriaUpdate<T> where(Predicate... restrictions) {
        setRestriction(restrictions);
        return this;
    }

    @Override
    public UpdateCriteriaBuilder<T> createCriteriaBuilder(EntityManager entityManager) {
        RenderContextImpl context = new RenderContextImpl();
        @SuppressWarnings("unchecked")
        UpdateCriteriaBuilder<T> updateCriteriaBuilder = criteriaBuilder.getCriteriaBuilderFactory()
                .update(entityManager, (Class<T>) getRoot().getJavaType(), getRoot().getAlias());

        context.setClauseType(RenderContext.ClauseType.SET);
        for (Assignment a : assignments) {
            String attribute = a.attributePath.getAttribute().getName();

            if (a.valueExpression instanceof LiteralExpression<?>) {
                Object value = ((LiteralExpression) a.valueExpression).getLiteral();
                updateCriteriaBuilder.set(attribute, value);
            } else {
                context.getBuffer().setLength(0);
                a.valueExpression.render(context);
                String valueExpression = context.takeBuffer();
                Map<String, InternalQuery<?>> aliasToSubqueries = context.takeAliasToSubqueryMap();

                if (aliasToSubqueries.isEmpty()) {
                    updateCriteriaBuilder.setExpression(attribute, valueExpression);
                } else {
                    MultipleSubqueryInitiator<?> initiator = updateCriteriaBuilder.setSubqueries(attribute, valueExpression);

                    for (Map.Entry<String, InternalQuery<?>> subqueryEntry : aliasToSubqueries.entrySet()) {
                        context.pushSubqueryInitiator(initiator.with(subqueryEntry.getKey()));
                        subqueryEntry.getValue().renderSubquery(context);
                        context.popSubqueryInitiator();
                    }

                    initiator.end();
                }
            }
        }

        renderWhere(updateCriteriaBuilder, context);

        for (ImplicitParameterBinding b : context.getImplicitParameterBindings()) {
            b.bind(updateCriteriaBuilder);
        }

        for (Map.Entry<String, ParameterExpression<?>> entry : context.getExplicitParameterNameMapping().entrySet()) {
            updateCriteriaBuilder.setParameterType(entry.getKey(), entry.getValue().getParameterType());
        }

        return updateCriteriaBuilder;
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static final class Assignment {
        private final SingularAttributePath<?> attributePath;
        private final AbstractExpression<?> valueExpression;

        public Assignment(SingularAttributePath<?> attributePath, AbstractExpression<?> valueExpression) {
            this.attributePath = attributePath;
            this.valueExpression = valueExpression;
        }
    }
}
