package com.blazebit.persistence.criteria;

import com.blazebit.persistence.Executable;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

public interface BlazeCriteriaDelete<T> extends CriteriaDelete<T>, BlazeCommonAbstractCriteria, Executable {

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
    public BlazeCriteriaDelete<T> where(Expression<Boolean> restriction);

    @Override
    public BlazeCriteriaDelete<T> where(Predicate... restrictions);

}
