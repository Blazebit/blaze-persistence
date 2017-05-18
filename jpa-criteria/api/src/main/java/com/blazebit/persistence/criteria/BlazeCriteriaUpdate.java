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

import com.blazebit.persistence.UpdateCriteriaBuilder;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

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
