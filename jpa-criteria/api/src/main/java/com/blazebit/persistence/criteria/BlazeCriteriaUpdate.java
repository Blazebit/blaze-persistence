package com.blazebit.persistence.criteria;

import com.blazebit.persistence.Executable;
import com.blazebit.persistence.Queryable;

import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

public interface BlazeCriteriaUpdate<T> extends CriteriaUpdate<T>, BlazeCommonAbstractCriteria, Executable {

    public BlazeRoot<T> from(Class<T> entityClass, String alias);

    public BlazeRoot<T> from(EntityType<T> entity, String alias);

    /* Covariant overrides */

    @Override
    public BlazeRoot<T> from(Class<T> entityClass);

    @Override
    public BlazeRoot<T> from(EntityType<T> entity);

    @Override
    public BlazeRoot<T> getRoot();

    @Override
    public <Y, X extends Y> BlazeCriteriaUpdate<T> set(SingularAttribute<? super T, Y> attribute, X value);

    @Override
    public <Y> BlazeCriteriaUpdate<T> set(SingularAttribute<? super T, Y> attribute, Expression<? extends Y> value);

    @Override
    public <Y, X extends Y> BlazeCriteriaUpdate<T> set(Path<Y> attribute, X value);

    @Override
    public <Y> BlazeCriteriaUpdate<T> set(Path<Y> attribute, Expression<? extends Y> value);

    @Override
    public BlazeCriteriaUpdate<T> set(String attributeName, Object value);

    @Override
    public BlazeCriteriaUpdate<T> where(Expression<Boolean> restriction);

    @Override
    public BlazeCriteriaUpdate<T> where(Predicate... restrictions);

}
