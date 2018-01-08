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

package com.blazebit.persistence;


import javax.persistence.EntityManager;

/**
 * A base interface for builders that support normal query functionality.
 * This interface is shared between the criteria builder and paginated criteria builder.
 *
 * @param <T> The query result type
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface QueryBuilder<T, X extends QueryBuilder<T, X>> extends BaseQueryBuilder<T, X>, Queryable<T, X> {

    /**
     * Returns the associated entity manager.
     *
     * @return The associated entity manager
     * @since 1.2.0
     */
    public EntityManager getEntityManager();
}
