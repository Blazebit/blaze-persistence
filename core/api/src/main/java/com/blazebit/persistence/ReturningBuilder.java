/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for the returning clause.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface ReturningBuilder<X extends ReturningBuilder<X>> {

    /**
     * Binds a entity attribute(<code>modificationQueryAttribute</code>) to a CTE attribute(<code>cteAttribute</code>) and returns this builder for chaining.
     *
     * @param cteAttribute The CTE attribute on which to bind
     * @param modificationQueryAttribute The attribute of the modification query entity which to return into the CTE attribute
     * @return This builder for chaining
     */
    public X returning(String cteAttribute, String modificationQueryAttribute);
    
}
