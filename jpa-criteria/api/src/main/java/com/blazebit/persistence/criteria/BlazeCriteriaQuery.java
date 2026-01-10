/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import com.blazebit.persistence.BaseCriteriaBuilder;
import com.blazebit.persistence.BaseQueryBuilder;
import com.blazebit.persistence.CriteriaBuilder;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import java.util.List;

/**
 * An extended version of {@link CriteriaQuery}.
 *
 * @param <T> the type of the defined result
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeCriteriaQuery<T> extends CriteriaQuery<T>, BlazeAbstractQuery<T> {

    /**
     * Create a Blaze-Persistence Core {@link CriteriaBuilder} from this query.
     *
     * @param entityManager The entity manager to which to bind the criteria builder
     * @return A new criteria builder
     */
    public CriteriaBuilder<T> createCriteriaBuilder(EntityManager entityManager);

    /**
     * Applies this criteria query onto an existing Blaze-Persistence Core {@link BaseCriteriaBuilder}.
     *
     * @param criteriaBuilder The criteria builder to apply this criteria query onto
     * @since 1.6.9
     */
    public void applyToCriteriaBuilder(BaseCriteriaBuilder<T, ?> criteriaBuilder);

    /**
     * The extended JPA {@link javax.persistence.criteria.CriteriaBuilder} associated with this query.
     *
     * @return The JPA {@link javax.persistence.criteria.CriteriaBuilder}
     */
    public BlazeCriteriaBuilder getCriteriaBuilder();

    /**
     * Like {@link CriteriaQuery#getOrderList()} but returns the subtype {@link BlazeOrder} instead.
     *
     * @return The list of ordering expressions
     */
    public List<BlazeOrder> getBlazeOrderList();
    
    /* Covariant overrides */

    @Override
    BlazeCriteriaQuery<T> select(Selection<? extends T> selection);

    @Override
    BlazeCriteriaQuery<T> multiselect(Selection<?>... selections);

    @Override
    BlazeCriteriaQuery<T> multiselect(List<Selection<?>> selectionList);

    @Override
    BlazeCriteriaQuery<T> where(Expression<Boolean> restriction);

    @Override
    BlazeCriteriaQuery<T> where(Predicate... restrictions);

    @Override
    BlazeCriteriaQuery<T> groupBy(Expression<?>... grouping);

    @Override
    BlazeCriteriaQuery<T> groupBy(List<Expression<?>> grouping);

    @Override
    BlazeCriteriaQuery<T> having(Expression<Boolean> restriction);

    @Override
    BlazeCriteriaQuery<T> having(Predicate... restrictions);

    @Override
    BlazeCriteriaQuery<T> orderBy(Order... o);

    @Override
    BlazeCriteriaQuery<T> orderBy(List<Order> o);

    @Override
    BlazeCriteriaQuery<T> distinct(boolean distinct);

}
