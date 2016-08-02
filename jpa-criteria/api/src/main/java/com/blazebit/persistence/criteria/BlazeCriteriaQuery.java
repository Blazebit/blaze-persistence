package com.blazebit.persistence.criteria;

import java.util.List;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.EntityType;

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
