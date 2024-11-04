/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

import java.io.Serializable;

/**
 * An interface that represents the key set of a row.
 * Instances of this interface can be used for key set pagination.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface Keyset extends Serializable {

    /**
     * Returns the key set tuple ordered by the respective order by expressions.
     *
     * @return The key set tuple for this keyset
     */
    public Serializable[] getTuple();
}
