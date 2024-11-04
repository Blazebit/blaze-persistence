/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A simple default implementation for the {@link Keyset} interface.
 *
 * @author Christian Beikov
 * @since 1.4.1
 */
public class DefaultKeyset implements Keyset {

    private static final long serialVersionUID = 1L;

    private final Serializable[] tuple;

    /**
     * Creates a new keyset object from the given tuple.
     *
     * @param tuple The tuple of the keyset
     */
    public DefaultKeyset(Serializable[] tuple) {
        this.tuple = tuple;
    }

    @Override
    public Serializable[] getTuple() {
        return tuple;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Arrays.deepHashCode(this.tuple);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Keyset && Arrays.deepEquals(this.tuple, ((Keyset) obj).getTuple());
    }
}
