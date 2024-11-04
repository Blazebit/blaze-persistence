/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.spi.type;

/**
 * A dirty tracker that exposes the captured initial state.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@SuppressWarnings("checkstyle:methodname")
public interface DirtyStateTrackable extends MutableStateTrackable {

    /**
     * Returns the initial state as array. Null if not partially updatable.
     * The order is the same as the metamodel attribute order of updatable attributes.
     * 
     * @return the initial state as array
     */
    public Object[] $$_getInitialState();

}
