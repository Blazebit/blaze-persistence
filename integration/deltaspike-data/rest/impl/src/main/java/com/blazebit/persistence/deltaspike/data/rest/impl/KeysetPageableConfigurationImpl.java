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

package com.blazebit.persistence.deltaspike.data.rest.impl;

import com.blazebit.persistence.deltaspike.data.KeysetPageRequest;
import com.blazebit.persistence.deltaspike.data.KeysetPageable;
import com.blazebit.persistence.deltaspike.data.Pageable;
import com.blazebit.persistence.deltaspike.data.rest.KeysetPageableConfiguration;

import javax.enterprise.inject.Typed;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Typed(KeysetPageableConfiguration.class)
public class KeysetPageableConfigurationImpl extends PageableConfigurationImpl implements KeysetPageableConfiguration {

    private static final String DEFAULT_PREVIOUS_OFFSET_PARAMETER = "prevOffset";
    private static final String DEFAULT_PREVIOUS_PAGE_PARAMETER = "prevPage";
    private static final String DEFAULT_PREVIOUS_SIZE_PARAMETER = "prevSize";
    private static final String DEFAULT_LOWEST_PARAMETER = "lowest";
    private static final String DEFAULT_HIGHEST_PARAMETER = "highest";
    private static final KeysetPageable DEFAULT_PAGE_REQUEST = new KeysetPageRequest(null, null, 0, 20);
    private KeysetPageable fallbackPageable = DEFAULT_PAGE_REQUEST;
    private String previousOffsetParameterName = DEFAULT_PREVIOUS_OFFSET_PARAMETER;
    private String previousPageParameterName = DEFAULT_PREVIOUS_PAGE_PARAMETER;
    private String previousSizeParameterName = DEFAULT_PREVIOUS_SIZE_PARAMETER;
    private String lowestParameterName = DEFAULT_LOWEST_PARAMETER;
    private String highestParameterName = DEFAULT_HIGHEST_PARAMETER;

    public KeysetPageableConfigurationImpl() {
    }

    public KeysetPageableConfigurationImpl(KeysetPageableConfiguration original) {
        super(original);
        this.fallbackPageable = original.getFallbackPageable();
        this.previousOffsetParameterName = original.getPreviousOffsetParameterName();
        this.previousPageParameterName = original.getPreviousPageParameterName();
        this.previousSizeParameterName = original.getPreviousSizeParameterName();
        this.lowestParameterName = original.getLowestParameterName();
        this.highestParameterName = original.getHighestParameterName();
    }

    @Override
    public KeysetPageable getFallbackPageable() {
        return fallbackPageable;
    }

    @Override
    public void setFallbackPageable(Pageable pageable) {
        setFallbackPageable((KeysetPageable) pageable);
    }

    @Override
    public String getPreviousOffsetParameterName() {
        return previousOffsetParameterName;
    }

    @Override
    public void setPreviousOffsetParameterName(String previousOffsetParameterName) {
        this.previousOffsetParameterName = previousOffsetParameterName;
    }

    @Override
    public void setFallbackPageable(KeysetPageable fallbackPageable) {
        this.fallbackPageable = fallbackPageable;
    }

    @Override
    public String getPreviousPageParameterName() {
        return previousPageParameterName;
    }

    @Override
    public void setPreviousPageParameterName(String previousPageParameterName) {
        this.previousPageParameterName = previousPageParameterName;
    }

    @Override
    public String getPreviousSizeParameterName() {
        return previousSizeParameterName;
    }

    @Override
    public void setPreviousSizeParameterName(String previousSizeParameterName) {
        this.previousSizeParameterName = previousSizeParameterName;
    }

    @Override
    public String getLowestParameterName() {
        return lowestParameterName;
    }

    @Override
    public void setLowestParameterName(String lowestParameterName) {
        this.lowestParameterName = lowestParameterName;
    }

    @Override
    public String getHighestParameterName() {
        return highestParameterName;
    }

    @Override
    public void setHighestParameterName(String highestParameterName) {
        this.highestParameterName = highestParameterName;
    }
}
