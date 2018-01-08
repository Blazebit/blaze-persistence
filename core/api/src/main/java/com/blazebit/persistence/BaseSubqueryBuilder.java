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

import javax.persistence.Tuple;

/**
 * A builder for subquery criteria queries.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface BaseSubqueryBuilder<X extends BaseSubqueryBuilder<X>> extends BaseQueryBuilder<Tuple, X>, GroupByBuilder<X>, DistinctBuilder<X>, LimitBuilder<X>, CTEBuilder<X> {

    /**
     * Like {@link BaseSubqueryBuilder#from(String, String)} with the
     * alias equivalent to the camel cased result of the class of the correlation parent.
     *
     * @param correlationPath The correlation path which should be queried
     * @return The query builder for chaining calls
     * @since 1.2.0
     */
    public X from(String correlationPath);

    /**
     * Sets the correlation path on which the query should be based on with the given alias.
     *
     * @param correlationPath The correlation path which should be queried
     * @param alias The alias for the entity
     * @return The query builder for chaining calls
     * @since 1.2.0
     */
    public X from(String correlationPath, String alias);
}
