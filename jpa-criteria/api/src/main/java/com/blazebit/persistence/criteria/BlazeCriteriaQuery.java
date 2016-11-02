/*
 * Copyright 2014 - 2016 Blazebit.
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

import java.util.List;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.Queryable;

public interface BlazeCriteriaQuery<T> extends CriteriaQuery<T>, BlazeAbstractQuery<T>, Queryable<T, BlazeCriteriaQuery<T>> {


    public CriteriaBuilder<T> createCriteriaBuilder();
    
    public BlazeCriteriaBuilder getCriteriaBuilder();

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
