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
 *
 * @param <T>
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public interface BlazeBaseCTECriteria<T> extends BlazeAbstractQuery<T> {


    /**
     * Like {@link BlazeBaseCTECriteria#getRoots()} but returns the subtype {@link BlazeRoot} instead.
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

    @Override
    BlazeBaseCTECriteria<T> where(Expression<Boolean> restriction);

    @Override
    BlazeBaseCTECriteria<T> where(Predicate... restrictions);

    @Override
    BlazeBaseCTECriteria<T> groupBy(Expression<?>... grouping);

    @Override
    BlazeBaseCTECriteria<T> groupBy(List<Expression<?>> grouping);

    @Override
    BlazeBaseCTECriteria<T> having(Expression<Boolean> restriction);

    @Override
    BlazeBaseCTECriteria<T> having(Predicate... restrictions);

    BlazeBaseCTECriteria<T> orderBy(Order... o);

    BlazeBaseCTECriteria<T> orderBy(List<Order> o);

    @Override
    BlazeBaseCTECriteria<T> distinct(boolean distinct);

    Set<ParameterExpression<?>> getParameters();

    // Path-like, except for expression stuff and plural attributes are irrelevant

    Bindable<T> getModel();

    <Y> Path<Y> get(SingularAttribute<? super T, Y> attribute);

    <Y> Path<Y> get(String attributeName);

}
