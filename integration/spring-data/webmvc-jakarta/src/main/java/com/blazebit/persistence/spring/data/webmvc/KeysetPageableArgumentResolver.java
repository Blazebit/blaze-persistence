/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.spring.data.webmvc;

import com.blazebit.persistence.spring.data.repository.KeysetPageable;
import com.blazebit.persistence.spring.data.webmvc.impl.KeysetPageableHandlerMethodArgumentResolver;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableArgumentResolver;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Argument resolver to extract a {@link KeysetPageable} object from a {@link NativeWebRequest} for a particular
 * {@link org.springframework.core.MethodParameter}. {@link KeysetPageable} resolution yields either
 * in a {@link Pageable} object or {@literal null} if {@link KeysetPageable} cannot be resolved.
 *
 * @author Moritz Becker
 * @author Eugen Mayer
 * @since 1.6.9
 */
public interface KeysetPageableArgumentResolver extends PageableArgumentResolver {
    /**
     * Configures the {@link Pageable} to be used as fallback in case no {@link PageableDefault} or
     * {@link PageableDefault} (the latter only supported in legacy mode) can be found at the method parameter to be
     * resolved.
     * <p>
     * If you set this to {@literal Optional#empty()}, be aware that you controller methods will get {@literal null}
     * handed into them in case no {@link Pageable} data can be found in the request. Note, that doing so will require you
     * supply bot the page <em>and</em> the size parameter with the requests as there will be no default for any of the
     * parameters available.
     *
     * @param fallbackPageable the {@link Pageable} to be used as general fallback.
     */
    void setFallbackPageable(Pageable fallbackPageable);

    /**
     * @param fallbackPageable the {@link KeysetPageable} to be used as general fallback.
     * @see KeysetPageableArgumentResolver#setFallbackPageable(Pageable)
     */
    void setFallbackPageable(KeysetPageable fallbackPageable);

    /**
     * Configures the maximum page size to be accepted. This allows to put an upper boundary of the page size to prevent
     * potential attacks trying to issue an {@link OutOfMemoryError}. Defaults to {@link org.springframework.data.web.PageableHandlerMethodArgumentResolver#DEFAULT_MAX_PAGE_SIZE}.
     *
     * @param maxPageSize the maxPageSize to set
     */
    void setMaxPageSize(int maxPageSize);

    /**
     * Configures the parameter name to be used to find the page number in the request. Defaults to {@code page}.
     *
     * @param pageParameterName the parameter name to be used, must not be {@literal null} or empty.
     */
    void setPageParameterName(String pageParameterName);

    /**
     * Configures the parameter name to be used to find the page size in the request. Defaults to {@code size}.
     *
     * @param sizeParameterName the parameter name to be used, must not be {@literal null} or empty.
     */
    void setSizeParameterName(String sizeParameterName);

    /**
     * Configures a general prefix to be prepended to the page number and page size parameters. Useful to namespace the
     * property names used in case they are clashing with ones used by your application. By default, no prefix is used.
     *
     * @param prefix the prefix to be used or {@literal null} to reset to the default.
     */
    void setPrefix(String prefix);

    /**
     * The delimiter to be used between the qualifier and the actual page number and size properties. Defaults to
     * {@code _}. So a qualifier of {@code foo} will result in a page number parameter of {@code foo_page}.
     *
     * @param qualifierDelimiter the delimiter to be used or {@literal null} to reset to the default.
     */
    void setQualifierDelimiter(String qualifierDelimiter);

    /**
     * Configures whether to expose and assume 1-based page number indexes in the request parameters. Defaults to
     * {@literal false}, meaning a page number of 0 in the request equals the first page. If this is set to
     * {@literal true}, a page number of 1 in the request will be considered the first page.
     *
     * @param oneIndexedParameters the oneIndexedParameters to set
     */
    void setOneIndexedParameters(boolean oneIndexedParameters);

    /**
     * Configures the parameter name to be used to find the offset in the request. Defaults to {@link KeysetPageableHandlerMethodArgumentResolver#DEFAULT_OFFSET_PARAMETER}.
     *
     * @param offsetParameterName the parameter name to be used, must not be {@literal null} or empty.
     */
    void setOffsetParameterName(String offsetParameterName);

    /**
     * Configures the parameter name to be used to find the previous offset in the request. Defaults to {@link KeysetPageableHandlerMethodArgumentResolver#DEFAULT_PREVIOUS_OFFSET_PARAMETER}.
     *
     * @param previousOffsetParameterName the parameter name to be used, must not be {@literal null} or empty.
     */
    void setPreviousOffsetParameterName(String previousOffsetParameterName);

    /**
     * Configures the parameter name to be used to find the previous page in the request. Defaults to {@link KeysetPageableHandlerMethodArgumentResolver#DEFAULT_PREVIOUS_PAGE_PARAMETER}.
     *
     * @param previousPageParameterName the parameter name to be used, must not be {@literal null} or empty.
     */
    void setPreviousPageParameterName(String previousPageParameterName);

    /**
     * Configures the parameter name to be used to find the previous size in the request. Defaults to {@link KeysetPageableHandlerMethodArgumentResolver#DEFAULT_PREVIOUS_SIZE_PARAMETER}.
     *
     * @param previousSizeParameterName the parameter name to be used, must not be {@literal null} or empty.
     */
    void setPreviousSizeParameterName(String previousSizeParameterName);

    /**
     * Configures the parameter name to be used to find the lowest keyset in the request. Defaults to {@link KeysetPageableHandlerMethodArgumentResolver#DEFAULT_LOWEST_PARAMETER}.
     *
     * @param lowestParameterName the parameter name to be used, must not be {@literal null} or empty.
     */
    void setLowestParameterName(String lowestParameterName);

    /**
     * Configures the parameter name to be used to find the highest keyset in the request. Defaults to {@link KeysetPageableHandlerMethodArgumentResolver#DEFAULT_HIGHEST_PARAMETER}.
     *
     * @param highestParameterName the parameter name to be used, must not be {@literal null} or empty.
     */
    void setHighestParameterName(String highestParameterName);
}
