/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence;

/**
 * An interface used to create subquery builders.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0
 */
public interface SubqueryInitiator<T> {

    /**
     * Like {@link SubqueryInitiator#from(java.lang.Class, java.lang.String)} with the
     * alias equivalent to the camel cased result of what {@link Class#getSimpleName()} of the entity class returns.
     *
     * @param entityClass The entity class which should be the root entity
     * @return A new subquery builder
     */
    public SubqueryBuilder<T> from(Class<?> entityClass);

    /**
     * Creates a new subquery builder with the given entity class as root entity in the FROM clause with the given alias.
     *
     * @param entityClass The entity class which should be the root entity
     * @param alias The alias for the root entity
     * @return A new subquery builder
     */
    public SubqueryBuilder<T> from(Class<?> entityClass, String alias);

    // TODO: documentation
    public StartOngoingSetOperationSubqueryBuilder<T, LeafOngoingSetOperationSubqueryBuilder<T>> startSet();
}
