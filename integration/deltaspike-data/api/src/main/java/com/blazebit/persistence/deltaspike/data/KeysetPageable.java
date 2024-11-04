/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data;

import com.blazebit.persistence.KeysetPage;

/**
 * Like {@link Pageable} but contains keyset information.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface KeysetPageable extends Pageable {

    /**
     * Returns the keyset page information.
     *
     * @return The keyset page
     */
    public KeysetPage getKeysetPage();

    /**
     * Returns the {@linkplain KeysetPageable} that can be used to request the next {@link Page}.
     *
     * @return The {@linkplain KeysetPageable} for the next {@linkplain Page}
     */
    @Override
    public KeysetPageable next();

    /**
     * Returns the {@linkplain KeysetPageable} that can be used to request the previous {@link Page} or null if the current one already is the first one.
     *
     * @return The {@linkplain KeysetPageable} for the previous {@linkplain Page}
     */
    @Override
    public KeysetPageable previous();

    /**
     * Returns the {@linkplain KeysetPageable} that can be used to request the previous {@link Page} or the first if the current one already is the first one.
     *
     * @return The {@linkplain KeysetPageable} for the previous or first {@linkplain Page}
     */
    @Override
    public KeysetPageable previousOrFirst();

    /**
     * Returns the {@linkplain KeysetPageable} that can be used to request the first {@link Page}.
     *
     * @return The {@linkplain KeysetPageable} for the first {@linkplain Page}
     */
    @Override
    public KeysetPageable first();

    /**
     * Returns whether count query execution is enabled or not.
     *
     * @return true when enabled, false otherwise
     * @since 1.4.0
     */
    public boolean isWithCountQuery();

    /**
     * Returns whether extraction for all keysets is enabled or not.
     *
     * @return true when enabled, false otherwise
     * @since 1.4.0
     */
    public boolean isWithExtractAllKeysets();
}
