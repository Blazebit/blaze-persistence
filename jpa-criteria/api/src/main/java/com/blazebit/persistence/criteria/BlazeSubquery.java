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

package com.blazebit.persistence.criteria;

import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;
import java.util.List;
import java.util.Set;

/**
 * An extended version of {@link Subquery}.
 *
 * @param <T> The type of the selection item
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeSubquery<T> extends Subquery<T>, BlazeAbstractQuery<T> {

    // TODO: think about whether multiselect support makes sense for subqueries

    /**
     * Like {@link BlazeSubquery#getOrderList()} but returns the subtype {@link BlazeOrder} instead.
     *
     * @return The list of ordering expressions
     */
    public List<BlazeOrder> getBlazeOrderList();

    /**
     * Return the ordering expressions in order of precedence.
     * Returns empty list if no ordering expressions have been
     * specified.
     * Modifications to the list do not affect the query.
     *
     * @return the list of ordering expressions
     */
    public List<Order> getOrderList();

    /**
     * Specify the ordering expressions that are used to
     * order the query results.
     * Replaces the previous ordering expressions, if any.
     * If no ordering expressions are specified, the previous
     * ordering, if any, is simply removed, and results will
     * be returned in no particular order.
     * The order of the ordering expressions in the list
     * determines the precedence, whereby the first element in the
     * list has highest precedence.
     *
     * @param orders zero or more ordering expressions
     * @return this for chaining
     */
    public BlazeSubquery<T> orderBy(Order... orders);

    /**
     * Like {@link BlazeSubquery#orderBy(Order...)} but accepts the subtype {@link BlazeOrder}.
     *
     * @param orders zero or more ordering expressions
     * @return this for chaining
     */
    public BlazeSubquery<T> orderBy(BlazeOrder... orders);

    /**
     * Specify the ordering expressions that are used to
     * order the query results.
     * Replaces the previous ordering expressions, if any.
     * If no ordering expressions are specified, the previous
     * ordering, if any, is simply removed, and results will
     * be returned in no particular order.
     * The order of the ordering expressions in the list
     * determines the precedence, whereby the first element in the
     * list has highest precedence.
     *
     * @param orderList list of zero or more ordering expressions
     * @return this for chaining
     */
    public BlazeSubquery<T> orderBy(List<BlazeOrder> orderList);

    /**
     * Returns the parameters defined on this query.
     *
     * @return The parameters
     */
    public Set<ParameterExpression<?>> getParameters();
    
    /* Compatibility for JPA 2.1 */

    /**
     * Returns the query (which may be a CriteriaQuery, CriteriaUpdate, CriteriaDelete, or a Subquery) of which this is a subquery.
     *
     * @return The enclosing query or subquery
     */
    public BlazeCommonAbstractCriteria getContainingQuery();

    /* Covariant overrides */

    @Override
    public BlazeSubquery<T> select(Expression<T> expression);

    @Override
    public BlazeSubquery<T> where(Expression<Boolean> restriction);

    @Override
    public BlazeSubquery<T> where(Predicate... restrictions);

    @Override
    public BlazeSubquery<T> groupBy(Expression<?>... grouping);

    @Override
    public BlazeSubquery<T> groupBy(List<Expression<?>> grouping);

    @Override
    public BlazeSubquery<T> having(Expression<Boolean> restriction);

    @Override
    public BlazeSubquery<T> having(Predicate... restrictions);

    @Override
    public BlazeSubquery<T> distinct(boolean distinct);

    @Override
    public <Y> BlazeRoot<Y> correlate(Root<Y> parentRoot);

    @Override
    public <X, Y> BlazeJoin<X, Y> correlate(Join<X, Y> parentJoin);

    @Override
    public <X, Y> BlazeCollectionJoin<X, Y> correlate(CollectionJoin<X, Y> parentCollection);

    @Override
    public <X, Y> BlazeSetJoin<X, Y> correlate(SetJoin<X, Y> parentSet);

    @Override
    public <X, Y> BlazeListJoin<X, Y> correlate(ListJoin<X, Y> parentList);

    @Override
    public <X, K, V> BlazeMapJoin<X, K, V> correlate(MapJoin<X, K, V> parentMap);

    @Override
    public BlazeAbstractQuery<?> getParent();

    @Override
    public <X> BlazeRoot<X> from(Class<X> entityClass);

    @Override
    public <X> BlazeRoot<X> from(EntityType<X> entity);

    @Override
    public <U> BlazeSubquery<U> subquery(Class<U> type);
}
