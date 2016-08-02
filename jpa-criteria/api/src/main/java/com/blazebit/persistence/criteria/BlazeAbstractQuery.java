package com.blazebit.persistence.criteria;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;
import java.util.List;
import java.util.Set;

// Compatibility for JPA 2.1
public interface BlazeAbstractQuery<T> extends AbstractQuery<T>, BlazeCommonAbstractCriteria {

    // TODO: create a fluent builder for clauses, maybe via RestrictionBuilder?
    // TODO: integrate support for SetBuilder, CteBuilder
    // TODO: integrate support for default join nodes?
    // TODO: maybe add explicit support for limit?

    public <X> BlazeRoot<X> from(Class<X> entityClass, String alias);

    public <X> BlazeRoot<X> from(EntityType<X> entityType, String alias);

    public Set<BlazeRoot<?>> getBlazeRoots();

    /* Covariant overrides */

    @Override
    <X> BlazeRoot<X> from(Class<X> entityClass);

    @Override
    <X> BlazeRoot<X> from(EntityType<X> entity);

    @Override
    BlazeAbstractQuery<T> where(Expression<Boolean> restriction);

    @Override
    BlazeAbstractQuery<T> where(Predicate... restrictions);

    @Override
    BlazeAbstractQuery<T> groupBy(Expression<?>... grouping);

    @Override
    BlazeAbstractQuery<T> groupBy(List<Expression<?>> grouping);

    @Override
    BlazeAbstractQuery<T> having(Expression<Boolean> restriction);

    @Override
    BlazeAbstractQuery<T> having(Predicate... restrictions);

    @Override
    BlazeAbstractQuery<T> distinct(boolean distinct);
}
