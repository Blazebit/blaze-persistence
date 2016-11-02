package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.DeleteCriteriaBuilder;
import com.blazebit.persistence.ModificationCriteriaBuilder;
import com.blazebit.persistence.criteria.BlazeCriteriaDelete;

import javax.persistence.Query;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import java.util.Map;

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
    public Query getQuery() {
        return createCriteriaBuilder().getQuery();
    }

    @Override
    public String getQueryString() {
        return createCriteriaBuilder().getQueryString();
    }

    @Override
    public int executeUpdate() {
        return createCriteriaBuilder().executeUpdate();
    }

    private ModificationCriteriaBuilder<?> createCriteriaBuilder() {
        RenderContextImpl context = new RenderContextImpl();
        DeleteCriteriaBuilder<? extends T> deleteCriteriaBuilder = criteriaBuilder.getCriteriaBuilderFactory()
                .delete(criteriaBuilder.getEntityManager(), getRoot().getJavaType(), getRoot().getAlias());

        renderWhere(deleteCriteriaBuilder, context);

        for (ImplicitParameterBinding b : context.getImplicitParameterBindings()) {
            b.bind(deleteCriteriaBuilder);
        }

        for (Map.Entry<String, ParameterExpression<?>> entry: context.getExplicitParameterNameMapping().entrySet()) {
            deleteCriteriaBuilder.setParameterType(entry.getKey(), entry.getValue().getParameterType());
        }

        return deleteCriteriaBuilder;
    }
}
