/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.repository;

import com.blazebit.persistence.KeysetPage;
import org.springframework.data.domain.Page;

/**
 * Like {@link Page} but contains keyset information.
 *
 * @param <T> Element type.
 *
 * @author Christian Beikov
 * @author Eugen Mayer
 * @since 1.6.9
 */
public interface KeysetAwarePage<T> extends Page<T>, KeysetAwareSlice<T> {

    /**
     * Returns the keyset page associated to the results of this page.
     *
     * @return The keyset page
     */
    public KeysetPage getKeysetPage();

    @Override
    KeysetPageable nextPageable();

    @Override
    KeysetPageable previousPageable();
}
