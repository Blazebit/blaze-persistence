package com.blazebit.persistence.criteria;

import java.util.List;
import java.util.Set;

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

public interface BlazeSubquery<T> extends Subquery<T>, BlazeAbstractQuery<T> {

    // TODO: think about whether multiselect support makes sense for subqueries

    public List<BlazeOrder> getBlazeOrderList();

    public List<Order> getOrderList();

    public BlazeSubquery<T> orderBy(Order... orders);

    public BlazeSubquery<T> orderBy(List<Order> orderList);

    public Set<ParameterExpression<?>> getParameters();
    
    /* Compatibility for JPA 2.1 */

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
