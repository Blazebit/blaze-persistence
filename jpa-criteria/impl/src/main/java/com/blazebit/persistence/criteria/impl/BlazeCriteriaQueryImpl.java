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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.criteria.BlazeCriteriaBuilder;
import com.blazebit.persistence.criteria.BlazeCriteriaQuery;
import com.blazebit.persistence.criteria.BlazeOrder;
import com.blazebit.persistence.criteria.BlazeRoot;
import com.blazebit.persistence.criteria.BlazeSubquery;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.EntityType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BlazeCriteriaQueryImpl<T> implements BlazeCriteriaQuery<T> {

    private final BlazeCriteriaBuilderImpl criteriaBuilder;

    private final Class<T> returnType;
    private final InternalQuery<T> query;

    public BlazeCriteriaQueryImpl(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> returnType) {
        this.criteriaBuilder = criteriaBuilder;
        this.returnType = returnType;
        this.query = new InternalQuery<T>(this, criteriaBuilder);
    }

    /* Select */

    @Override
    public boolean isDistinct() {
        return query.isDistinct();
    }

    @Override
    public BlazeCriteriaQuery<T> distinct(boolean distinct) {
        query.setDistinct(distinct);
        return this;
    }

    @Override
    public BlazeCriteriaQuery<T> select(Selection<? extends T> selection) {
        query.setSelection(criteriaBuilder.wrapSelection(selection));
        return this;
    }

    @Override
    public Selection<T> getSelection() {
        return query.getSelection();
    }

    @Override
    public BlazeCriteriaQuery<T> multiselect(Selection<?>... selections) {
        return multiselect(Arrays.asList(selections));
    }

    @Override
    @SuppressWarnings("unchecked")
    public BlazeCriteriaQuery<T> multiselect(List<Selection<?>> selections) {
        final Selection<? extends T> selection;

        if (Tuple.class.isAssignableFrom(getResultType())) {
            selection = (Selection<? extends T>) criteriaBuilder.tuple(selections);
        } else if (getResultType().isArray()) {
            selection = (Selection<? extends T>) (Selection<?>) criteriaBuilder.array((Class<? extends Object[]>) (Class<?>) getResultType(), selections);
        } else if (Object.class.equals(getResultType())) {
            switch (selections.size()) {
                case 0: {
                    throw new IllegalArgumentException("empty selections passed to criteria query typed as Object");
                }
                case 1: {
                    selection = (Selection<? extends T>) selections.get(0);
                    break;
                }
                default: {
                    selection = (Selection<? extends T>) criteriaBuilder.array(selections);
                }
            }
        } else {
            selection = criteriaBuilder.construct(getResultType(), selections);
        }

        query.setSelection(selection);
        return this;
    }

    /* From */

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

    /* Where */

    @Override
    public Predicate getRestriction() {
        return query.getRestriction();
    }

    @Override
    public BlazeCriteriaQuery<T> where(Expression<Boolean> restriction) {
        query.setRestriction(restriction == null ? null : criteriaBuilder.wrap(restriction));
        return this;
    }

    @Override
    public BlazeCriteriaQuery<T> where(Predicate... restrictions) {
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
    public BlazeCriteriaQuery<T> groupBy(Expression<?>... groupings) {
        if (groupings == null || groupings.length == 0) {
            query.setGroupList(Collections.EMPTY_LIST);
        } else {
            query.setGroupList(Arrays.asList(groupings));
        }

        return this;
    }

    @Override
    public BlazeCriteriaQuery<T> groupBy(List<Expression<?>> groupings) {
        query.setGroupList(groupings);
        return this;
    }

    /* Having */

    @Override
    public Predicate getGroupRestriction() {
        return query.getGroupRestriction();
    }

    @Override
    public BlazeCriteriaQuery<T> having(Expression<Boolean> restriction) {
        if (restriction == null) {
            query.setHaving(null);
        } else {
            query.setHaving(criteriaBuilder.wrap(restriction));
        }
        return this;
    }

    @Override
    public BlazeCriteriaQuery<T> having(Predicate... restrictions) {
        if (restrictions == null || restrictions.length == 0) {
            query.setHaving(null);
        } else {
            query.setHaving(criteriaBuilder.and(restrictions));
        }
        return this;
    }

    /* Order by */

    @Override
    public List<BlazeOrder> getBlazeOrderList() {
        return query.getBlazeOrderList();
    }

    @Override
    public List<Order> getOrderList() {
        return query.getOrderList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public BlazeCriteriaQuery<T> orderBy(Order... orders) {
        if (orders == null || orders.length == 0) {
            query.setOrderList(Collections.EMPTY_LIST);
        } else {
            query.setOrderList(Arrays.asList(orders));
        }

        return this;
    }

    @Override
    public BlazeCriteriaQuery<T> orderBy(List<Order> orderList) {
        query.setOrderList(orderList);
        return this;
    }

    /* Parameters */

    @Override
    public Set<ParameterExpression<?>> getParameters() {
        return query.getParameters();
    }

    /* Subquery */

    @Override
    public <U> BlazeSubquery<U> subquery(Class<U> type) {
        return query.subquery(type);
    }

    @Override
    public Class<T> getResultType() {
        return returnType;
    }

    @Override
    public BlazeCriteriaBuilder getCriteriaBuilder() {
        return criteriaBuilder;
    }

    @Override
    public CriteriaBuilder<T> createCriteriaBuilder(EntityManager entityManager) {
        CriteriaBuilder<T> cb = criteriaBuilder.getCriteriaBuilderFactory().create(entityManager, returnType);
        return query.render(cb);
    }

}
