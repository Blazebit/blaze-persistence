/*
 * Copyright 2014 - 2019 Blazebit.
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

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;
import java.util.Set;

/**
 * A modified {@link BlazeCriteriaBuilder} for CTE's.
 *
 * @param <T> the entity type that is the entity of the cTE
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public interface BlazeCTECriteria<T> extends BlazeAbstractQuery<T> {

    /**
     * Like {@link BlazeCTECriteria#getRoots()} but returns the subtype {@link BlazeRoot} instead.
     *
     * @return the set of query roots
     */
    Set<BlazeRoot<?>> getBlazeRoots();

    /**
     * Like {@link CriteriaQuery#getOrderList()} but returns the subtype {@link BlazeOrder} instead.
     *
     * @return The list of ordering expressions
     */
    List<BlazeOrder> getBlazeOrderList();


    Set<Root<?>> getRoots();

    /**
     * Update the value of the specified attribute.
     *
     * @param attribute attribute to be updated
     * @param value new value
     *
     * @return the modified query
     */
    <Y, X extends Y> BlazeCTECriteria<T> set(SingularAttribute<? super T, Y> attribute, X value);

    /**
     * Update the value of the specified attribute.
     *
     * @param attribute attribute to be updated
     * @param value new value
     *
     * @return the modified query
     */
    <Y> BlazeCTECriteria<T> set(SingularAttribute<? super T, Y> attribute, Expression<? extends Y> value);

    /**
     * Update the value of the specified attribute.
     *
     * @param attribute attribute to be updated
     * @param value new value
     *
     * @return the modified query
     */
    <Y, X extends Y> BlazeCTECriteria<T> set(Path<Y> attribute, X value);

    /**
     * Update the value of the specified attribute.
     *
     * @param attribute attribute to be updated
     * @param value new value
     *
     * @return the modified query
     */
    <Y> BlazeCTECriteria<T> set(Path<Y> attribute, Expression<? extends Y> value);

    /**
     * Update the value of the specified attribute.
     *
     * @param attributeName name of the attribute to be updated
     * @param value new value
     *
     * @return the modified query
     */
    BlazeCTECriteria<T> set(String attributeName, Object value);

    @Override
    BlazeCTECriteria<T> where(Expression<Boolean> restriction);

    @Override
    BlazeCTECriteria<T> where(Predicate... restrictions);

    @Override
    BlazeCTECriteria<T> groupBy(Expression<?>... grouping);

    @Override
    BlazeCTECriteria<T> groupBy(List<Expression<?>> grouping);

    @Override
    BlazeCTECriteria<T> having(Expression<Boolean> restriction);

    @Override
    BlazeCTECriteria<T> having(Predicate... restrictions);

    BlazeCTECriteria<T> orderBy(Order... o);

    BlazeCTECriteria<T> orderBy(List<Order> o);

    @Override
    BlazeCTECriteria<T> distinct(boolean distinct);

    Set<ParameterExpression<?>> getParameters();

    // Path-like, except for expression stuff and plural attributes are irrelevant

    Bindable<T> getModel();

    <Y> Path<Y> get(SingularAttribute<? super T, Y> attribute);

    <Y> Path<Y> get(String attributeName);

}
