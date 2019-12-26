package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.criteria.BlazeCTECriteria;
import com.blazebit.persistence.criteria.BlazeCriteriaQuery;
import com.blazebit.persistence.criteria.BlazeRoot;
import com.blazebit.persistence.criteria.BlazeSubquery;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BlazeCTECriteriaImpl<T> implements BlazeCTECriteria<T> {

    private final BlazeCriteriaBuilderImpl criteriaBuilder;

    private final Class<T> returnType;
    private final InternalQuery<T> query;

    public BlazeCTECriteriaImpl(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> returnType) {
        this.criteriaBuilder = criteriaBuilder;
        this.returnType = returnType;
        this.query = new InternalQuery<T>(this, criteriaBuilder);
    }

    @Override
    public <X> BlazeCTECriteria<X> with(Class<X> clasz) {
        return null;
    }

    @Override
    public Set<Root<?>> getRoots() {
        return query.getRoots();
    }

    @Override
    public Set<BlazeRoot<?>> getBlazeRoots() {
        return query.getBlazeRoots();
    }

    @Override
    public <X> BlazeRoot<X> from(Class<X> entityClass) {
        return query.from(entityClass, null);
    }

    @Override
    public <X> BlazeRoot<X> from(EntityType<X> entityType) {
        return query.from(entityType, null);
    }

    @Override
    public <X> BlazeRoot<X> from(Class<X> entityClass, String alias) {
        return query.from(entityClass, alias);
    }

    @Override
    public <X> BlazeRoot<X> from(EntityType<X> entityType, String alias) {
        return query.from(entityType, alias);
    }

    @Override
    public Selection<T> getSelection() {
        return query.getSelection();
    }

    @Override
    public Class<T> getResultType() {
        return returnType;
    }

    @Override
    public <Y, X extends Y> BlazeCTECriteria<T> set(SingularAttribute<? super T, Y> attribute, X value) {
        return null;
    }

    @Override
    public <Y> BlazeCTECriteria<T> set(SingularAttribute<? super T, Y> attribute, Expression<? extends Y> value) {
        return null;
    }

    @Override
    public <Y, X extends Y> BlazeCTECriteria<T> set(Path<Y> attribute, X value) {
        return null;
    }

    @Override
    public <Y> BlazeCTECriteria<T> set(Path<Y> attribute, Expression<? extends Y> value) {
        return null;
    }

    @Override
    public BlazeCTECriteria<T> set(String attributeName, Object value) {
        return null;
    }

    /* Where */

    @Override
    public Predicate getRestriction() {
        return query.getRestriction();
    }

    @Override
    public BlazeCTECriteriaImpl<T> where(Expression<Boolean> restriction) {
        query.setRestriction(restriction == null ? null : criteriaBuilder.wrap(restriction));
        return this;
    }

    @Override
    public BlazeCTECriteria<T> where(Predicate... restrictions) {
        if (restrictions == null || restrictions.length == 0) {
            query.setRestriction(null);
        } else {
            query.setRestriction(criteriaBuilder.and(restrictions));
        }
        return this;
    }

    /* Group by */

    @Override
    public List<Expression<?>> getGroupList() {
        return query.getGroupList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public BlazeCTECriteria<T> groupBy(Expression<?>... groupings) {
        if (groupings == null || groupings.length == 0) {
            query.setGroupList(Collections.EMPTY_LIST);
        } else {
            query.setGroupList(Arrays.asList(groupings));
        }

        return this;
    }

    @Override
    public BlazeCTECriteria<T> groupBy(List<Expression<?>> groupings) {
        query.setGroupList(groupings);
        return this;
    }

    /* Having */

    @Override
    public Predicate getGroupRestriction() {
        return query.getGroupRestriction();
    }

    @Override
    public BlazeCTECriteria<T> having(Expression<Boolean> restriction) {
        if (restriction == null) {
            query.setHaving(null);
        } else {
            query.setHaving(criteriaBuilder.wrap(restriction));
        }
        return this;
    }

    @Override
    public BlazeCTECriteria<T> having(Predicate... restrictions) {
        if (restrictions == null || restrictions.length == 0) {
            query.setHaving(null);
        } else {
            query.setHaving(criteriaBuilder.and(restrictions));
        }
        return this;
    }

    @Override
    public boolean isDistinct() {
        return query.isDistinct();
    }

    @Override
    public BlazeCTECriteria<T> distinct(boolean distinct) {
        query.setDistinct(distinct);
        return this;
    }

    @Override
    public <U> BlazeSubquery<U> subquery(Class<U> type) {
        return query.subquery(type);
    }

}
