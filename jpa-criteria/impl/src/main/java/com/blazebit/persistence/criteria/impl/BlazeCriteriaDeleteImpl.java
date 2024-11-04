/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.DeleteCriteriaBuilder;
import com.blazebit.persistence.criteria.BlazeCriteriaDelete;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BlazeCriteriaDeleteImpl<T> extends AbstractModificationCriteriaQuery<T> implements BlazeCriteriaDelete<T> {

    public BlazeCriteriaDeleteImpl(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> targetEntity, String alias) {
        super(criteriaBuilder);
        from(targetEntity, alias);
    }

    @Override
    public BlazeCriteriaDelete<T> where(Expression<Boolean> restriction) {
        setRestriction(restriction);
        return this;
    }

    @Override
    public BlazeCriteriaDelete<T> where(Predicate... restrictions) {
        setRestriction(restrictions);
        return this;
    }

    @Override
    public DeleteCriteriaBuilder<T> createCriteriaBuilder(EntityManager entityManager) {
        RenderContextImpl context = new RenderContextImpl();
        @SuppressWarnings("unchecked")
        DeleteCriteriaBuilder<T> deleteCriteriaBuilder = criteriaBuilder.getCriteriaBuilderFactory()
                .delete(entityManager, (Class<T>) getRoot().getJavaType(), getRoot().getAlias());

        renderWhere(deleteCriteriaBuilder, context);

        for (ImplicitParameterBinding b : context.getImplicitParameterBindings()) {
            b.bind(deleteCriteriaBuilder);
        }

        for (Map.Entry<ParameterExpression<?>, String> entry : context.getExplicitParameterMapping().entrySet()) {
            deleteCriteriaBuilder.registerCriteriaParameter(entry.getValue(), entry.getKey());
        }

        return deleteCriteriaBuilder;
    }
}
