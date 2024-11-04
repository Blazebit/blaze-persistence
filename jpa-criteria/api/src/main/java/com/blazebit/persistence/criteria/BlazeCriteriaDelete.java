/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import com.blazebit.persistence.DeleteCriteriaBuilder;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;

/**
 * An extended version of {@link CriteriaDelete}.
 *
 * @param <T> The entity type that is the target of the delete
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeCriteriaDelete<T> extends CriteriaDelete<T>, BlazeCommonAbstractCriteria {

    /**
     * Create a Blaze-Persistence Core {@link DeleteCriteriaBuilder} from this query.
     *
     * @param entityManager The entity manager to which to bind the criteria builder
     * @return A new criteria builder
     * @since 1.3.0
     */
    public DeleteCriteriaBuilder<T> createCriteriaBuilder(EntityManager entityManager);

    /**
     * Like {@link CriteriaDelete#from(Class)} but allows to set the alias of the {@link BlazeRoot}.
     *
     * @param entityClass the entity class
     * @param alias       The alias for the {@link BlazeRoot}
     * @return query root corresponding to the given entity
     */
    public BlazeRoot<T> from(Class<T> entityClass, String alias);

    /**
     * Like {@link CriteriaDelete#from(EntityType)} but allows to set the alias of the {@link BlazeRoot}.
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
    public BlazeCriteriaDelete<T> where(Expression<Boolean> restriction);

    @Override
    public BlazeCriteriaDelete<T> where(Predicate... restrictions);

}
