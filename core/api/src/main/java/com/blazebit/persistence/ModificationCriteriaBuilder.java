/*
 * Copyright 2014 - 2016 Blazebit.
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

import javax.persistence.Tuple;
import javax.persistence.TypedQuery;

/**
 * A builder for modification queries.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface ModificationCriteriaBuilder<X extends ModificationCriteriaBuilder<X>> extends Executable, CommonQueryBuilder<X>, BaseModificationCriteriaBuilder<X>, CTEBuilder<X> {

    /**
     * Returns the query root
     *
     * @return The root of this query
     * @since 1.2.0
     */
    public Root getRoot();

    // TODO: documentation
    public ReturningResult<Tuple> executeWithReturning(String... attributes);

    // TODO: documentation
    public <T> ReturningResult<T> executeWithReturning(String attribute, Class<T> type);

    // TODO: documentation
    public <T> ReturningResult<T> executeWithReturning(ReturningObjectBuilder<T> objectBuilder);

    // TODO: documentation
    public TypedQuery<ReturningResult<Tuple>> getWithReturningQuery(String... attributes);

    // TODO: documentation
    public <T> TypedQuery<ReturningResult<T>> getWithReturningQuery(String attribute, Class<T> type);

    // TODO: documentation
    public <T> TypedQuery<ReturningResult<T>> getWithReturningQuery(ReturningObjectBuilder<T> objectBuilder);
}
