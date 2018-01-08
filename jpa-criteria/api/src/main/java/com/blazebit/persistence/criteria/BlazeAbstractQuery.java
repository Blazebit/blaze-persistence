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

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;
import java.util.List;
import java.util.Set;

/**
 * An extended version of {@link AbstractQuery} that allows setting an alias for {@link javax.persistence.criteria.From} elements.
 *
 * @param <T> the type of the result
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeAbstractQuery<T> extends AbstractQuery<T>, BlazeCommonAbstractCriteria {

    // TODO: create a fluent builder for clauses, maybe via RestrictionBuilder?
    // TODO: integrate support for SetBuilder, CteBuilder
    // TODO: integrate support for default join nodes?
    // TODO: maybe add explicit support for limit?

    /**
     * Like {@link AbstractQuery#from(Class)} but allows to set the alias of the {@link BlazeRoot}.
     *
     * @param entityClass the entity class
     * @param alias       The alias for the {@link BlazeRoot}
     * @param <X>         The entity type
     * @return query root corresponding to the given entity
     */
    public <X> BlazeRoot<X> from(Class<X> entityClass, String alias);

    /**
     * Like {@link AbstractQuery#from(EntityType)} but allows to set the alias of the {@link BlazeRoot}.
     *
     * @param entityType the entity type
     * @param alias      The alias for the {@link BlazeRoot}
     * @param <X>        The entity type
     * @return query root corresponding to the given entity
     */
    public <X> BlazeRoot<X> from(EntityType<X> entityType, String alias);

    /**
     * Like {@link AbstractQuery#getRoots()} but returns the subtype {@link BlazeRoot} instead.
     *
     * @return the set of query roots
     */
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
