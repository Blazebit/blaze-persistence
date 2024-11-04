/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import jakarta.persistence.criteria.Fetch;

/**
 * An extended version of {@link Fetch}.
 *
 * @param <Z> The source type of the fetch
 * @param <X> The target type of the fetch
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeFetch<Z, X> extends Fetch<Z, X>, BlazeFetchParent<Z, X> {

    /* Covariant overrides */

    /**
     * Like {@link Fetch#getParent()} but returns the subtype {@link BlazeFetchParent} instead.
     *
     * @return fetch parent
     */
    BlazeFetchParent<?, Z> getParent();

}
