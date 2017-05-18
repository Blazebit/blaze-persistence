/*
 * Copyright 2014 - 2018 Blazebit.
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
