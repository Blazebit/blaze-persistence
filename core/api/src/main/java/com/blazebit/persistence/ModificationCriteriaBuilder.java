/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;

/**
 * A builder for modification queries.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface ModificationCriteriaBuilder<X extends ModificationCriteriaBuilder<X>> extends Executable, CommonQueryBuilder<X>, BaseModificationCriteriaBuilder<X>, CTEBuilder<X> {

    /**
     * Returns the query root.
     *
     * @return The root of this query
     * @since 1.2.0
     */
    public From getRoot();

    /**
     * Executes the modification query and returns the given attributes as tuples.
     *
     * @param attributes The attributes of a changed entity to return
     * @return A result wrapper containing the update count and the new values of the attributes
     */
    public ReturningResult<Tuple> executeWithReturning(String... attributes);

    /**
     * Executes the modification query and returns the given attribute with the specified type.
     *
     * @param attribute The attribute of a changed entity to return
     * @param type The type of the attribute
     * @param <T> The result type of the attribute
     * @return A result wrapper containing the update count and the new value of the attribute
     */
    public <T> ReturningResult<T> executeWithReturning(String attribute, Class<T> type);

    /**
     * Executes the modification query and returns an object consisting of the attributes applied by the object builder.
     *
     * @param objectBuilder The object builder that applies attributes and constructs the result objects
     * @param <T> The type of the object constructed by the object builder
     * @return A result wrapper containing the update count and the objects constructed by the obbject builder
     */
    public <T> ReturningResult<T> executeWithReturning(ReturningObjectBuilder<T> objectBuilder);

    /**
     * Creates a query that contains the modification query and returns the given attributes as tuples.
     *
     * @param attributes The attributes of a changed entity to return
     * @return A result wrapper containing the update count and the new values of the attributes
     */
    public TypedQuery<ReturningResult<Tuple>> getWithReturningQuery(String... attributes);

    /**
     * Creates a query that contains the modification query and returns the given attribute with the specified type.
     *
     * @param attribute The attribute of a changed entity to return
     * @param type The type of the attribute
     * @param <T> The result type of the attribute
     * @return A result wrapper containing the update count and the new value of the attribute
     */
    public <T> TypedQuery<ReturningResult<T>> getWithReturningQuery(String attribute, Class<T> type);

    /**
     * Creates a query that contains the modification query and returns an object consisting of the attributes applied by the object builder.
     *
     * @param objectBuilder The object builder that applies attributes and constructs the result objects
     * @param <T> The type of the object constructed by the object builder
     * @return A result wrapper containing the update count and the objects constructed by the obbject builder
     */
    public <T> TypedQuery<ReturningResult<T>> getWithReturningQuery(ReturningObjectBuilder<T> objectBuilder);
}
