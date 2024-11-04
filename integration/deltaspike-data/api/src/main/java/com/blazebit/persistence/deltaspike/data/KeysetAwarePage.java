/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data;

import com.blazebit.persistence.KeysetPage;

/**
 * Like {@link Page} but contains keyset information.
 *
 * @param <T> Element type.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface KeysetAwarePage<T> extends Page<T>, KeysetAwareSlice<T> {

    /**
     * Returns the keyset page associated to the results of this page.
     *
     * @return The keyset page
     */
    public KeysetPage getKeysetPage();

    @Override
    KeysetPageable currentPageable();

    @Override
    KeysetPageable nextPageable();

    @Override
    KeysetPageable previousPageable();
}
