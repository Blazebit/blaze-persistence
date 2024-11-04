/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for the returning clause.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface SimpleReturningBuilder {

    /**
     * Adds the given entity attribute(<code>modificationQueryAttribute</code>) to the <code>RETURNING</code> clause.
     *
     * @param modificationQueryAttribute The attribute of the modification query entity which to return
     * @return This builder for chaining
     */
    public SimpleReturningBuilder returning(String modificationQueryAttribute);
    
}
