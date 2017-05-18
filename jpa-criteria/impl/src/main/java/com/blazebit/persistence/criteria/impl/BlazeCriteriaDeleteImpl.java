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

        for (Map.Entry<String, ParameterExpression<?>> entry : context.getExplicitParameterNameMapping().entrySet()) {
            deleteCriteriaBuilder.setParameterType(entry.getKey(), entry.getValue().getParameterType());
        }

        return deleteCriteriaBuilder;
    }
}
