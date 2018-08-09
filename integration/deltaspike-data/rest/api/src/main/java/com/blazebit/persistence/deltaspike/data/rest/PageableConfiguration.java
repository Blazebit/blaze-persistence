/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.deltaspike.data.rest;

import com.blazebit.persistence.deltaspike.data.Pageable;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface PageableConfiguration {

    /**
     * Returns the pageable to fall back to when no page configuration is given.
     *
     * @return The fallback pageable
     */
    public Pageable getFallbackPageable();

    /**
     * Sets the pageable to use when no page configuration is given.
     *
     * @param fallbackPageable The fallback pageable
     */
    public void setFallbackPageable(Pageable fallbackPageable);

    /**
     * Returns the name of the query parameter to use for the <em>offset</em>.
     *
     * @return The query parameter name for the offset
     * @since 1.3.0
     */
    public String getOffsetParameterName();

    /**
     * Sets the name of the query parameter that should be used to extract the <em>offset</em> value.
     *
     * @param offsetParameterName The query parameter name for the offset
     * @since 1.3.0
     */
    public void setOffsetParameterName(String offsetParameterName);

    /**
     * Returns the name of the query parameter to use for the <em>page</em>.
     *
     * @return The query parameter name for the page
     */
    public String getPageParameterName();

    /**
     * Sets the name of the query parameter that should be used to extract the <em>page</em> value.
     *
     * @param pageParameterName The query parameter name for the page
     */
    public void setPageParameterName(String pageParameterName);

    /**
     * Returns the name of the query parameter to use for the <em>pageSize</em>.
     *
     * @return The query parameter name for the page size
     */
    public String getSizeParameterName();

    /**
     * Sets the name of the query parameter that should be used to extract the <em>pageSize</em> value.
     *
     * @param sizeParameterName The query parameter name for the page size
     */
    public void setSizeParameterName(String sizeParameterName);

    /**
     * Returns the name of the query parameter to use for the <em>sort</em>.
     *
     * @return The query parameter name for the sort
     */
    public String getSortParameterName();

    /**
     * Sets the name of the query parameter that should be used to extract the <em>sort</em> value.
     *
     * @param sortParameterName The query parameter name for the sort
     */
    public void setSortParameterName(String sortParameterName);

    /**
     * Returns the name prefix to use when extracting query parameters.
     *
     * @return The query parameter name prefix
     */
    public String getPrefix();

    /**
     * Sets the query parameter name prefix to use for extracting query parameters.
     *
     * @param prefix The query parameter name prefix
     */
    public void setPrefix(String prefix);

    /**
     * Returns the allowed maximum page size.
     *
     * @return The allowed maximum page size
     */
    public int getMaxPageSize();

    /**
     * Sets the allowed maximum page size.
     *
     * @param maxPageSize The allowed maximum page size
     */
    public void setMaxPageSize(int maxPageSize);

    /**
     * Returns whether the page parameter is 1-based rather than 0-based.
     *
     * @return <code>true</code> if page is 1-based, false if 0-based
     */
    public boolean isOneIndexedParameters();

    /**
     * Sets whether the page parameter is 1-based or 0-based.
     *
     * @param oneIndexedParameters <code>true</code> if 1-based, false if 0-based
     */
    public void setOneIndexedParameters(boolean oneIndexedParameters);
}
