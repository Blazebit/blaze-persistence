/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import com.blazebit.persistence.UpdateCriteriaBuilder;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;

/**
 * An extended version of {@link CriteriaUpdate}.
 *
 * @param <T> the entity type that is the target of the update
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeCriteriaUpdate<T> extends CriteriaUpdate<T>, BlazeCommonAbstractCriteria {

    /**
     * Create a Blaze-Persistence Core {@link UpdateCriteriaBuilder} from this query.
     *
     * @param entityManager The entity manager to which to bind the criteria builder
     * @return A new criteria builder
     * @since 1.3.0
     */
    public UpdateCriteriaBuilder<T> createCriteriaBuilder(EntityManager entityManager);

    /**
     * Like {@link CriteriaUpdate#from(Class)} but allows to set the alias of the {@link BlazeRoot}.
     *
     * @param entityClass the entity class
     * @param alias       The alias for the {@link BlazeRoot}
     * @return query root corresponding to the given entity
     */
    public BlazeRoot<T> from(Class<T> entityClass, String alias);

    /**
     * Like {@link CriteriaUpdate#from(EntityType)} but allows to set the alias of the {@link BlazeRoot}.
     *
     * @param entityType the entity type
     * @param alias      The alias for the {@link BlazeRoot}
     * @return query root corresponding to the given entity
     */
    public BlazeRoot<T> from(EntityType<T> entityType, String alias);

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
