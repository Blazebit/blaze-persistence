/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.repository;

import com.blazebit.persistence.KeysetPage;
import org.springframework.data.domain.Pageable;

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

    /**
     * Returns the offset as int.
     *
     * @return The offset as int
     * @since 1.3.0
     */
    public int getIntOffset();
}
