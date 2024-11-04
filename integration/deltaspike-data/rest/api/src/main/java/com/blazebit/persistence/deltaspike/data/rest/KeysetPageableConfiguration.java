/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.rest;

import com.blazebit.persistence.deltaspike.data.KeysetPageable;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface KeysetPageableConfiguration extends PageableConfiguration {

    @Override
    public KeysetPageable getFallbackPageable();

    /**
     * Sets the pageable to use when no page configuration is given.
     *
     * @param fallbackPageable The fallback pageable
     */
    public void setFallbackPageable(KeysetPageable fallbackPageable);

    /**
     * Returns the name of the query parameter to use for the <em>previous offset</em>.
     *
     * @return The query parameter name for the previous offset
     * @since 1.3.0
     */
    public String getPreviousOffsetParameterName();

    /**
     * Sets the name of the query parameter that should be used to extract the <em>previous offset</em> value.
     *
     * @param previousOffsetParameterName The query parameter name for the previous offset
     * @since 1.3.0
     */
    public void setPreviousOffsetParameterName(String previousOffsetParameterName);

    /**
     * Returns the name of the query parameter to use for the <em>previous page</em>.
     *
     * @return The query parameter name for the previous page
     */
    public String getPreviousPageParameterName();

    /**
     * Sets the name of the query parameter that should be used to extract the <em>previous page</em> value.
     *
     * @param previousPageParameterName The query parameter name for the previous page
     */
    public void setPreviousPageParameterName(String previousPageParameterName);

    /**
     * Returns the name of the query parameter to use for the <em>previous page size</em>.
     *
     * @return The query parameter name for the previous page size
     */
    public String getPreviousSizeParameterName();

    /**
     * Sets the name of the query parameter that should be used to extract the <em>previous page size</em> value.
     *
     * @param previousSizeParameterName The query parameter name for the previous page size
     */
    public void setPreviousSizeParameterName(String previousSizeParameterName);

    /**
     * Returns the name of the query parameter to use for the <em>lowest keyset</em>.
     *
     * @return The query parameter name for the lowest keyset
     */
    public String getLowestParameterName();

    /**
     * Sets the name of the query parameter that should be used to extract the <em>lowest keyset</em> value.
     *
     * @param lowestParameterName The query parameter name for the lowest keyset
     */
    public void setLowestParameterName(String lowestParameterName);

    /**
     * Returns the name of the query parameter to use for the <em>highest keyset</em>.
     *
     * @return The query parameter name for the highest keyset
     */
    public String getHighestParameterName();

    /**
     * Sets the name of the query parameter that should be used to extract the <em>highest keyset</em> value.
     *
     * @param highestParameterName The query parameter name for the highest keyset
     */
    public void setHighestParameterName(String highestParameterName);
}
