/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
