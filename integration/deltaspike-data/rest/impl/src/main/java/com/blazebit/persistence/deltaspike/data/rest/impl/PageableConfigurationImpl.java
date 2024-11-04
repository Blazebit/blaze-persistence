/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.rest.impl;

import com.blazebit.persistence.deltaspike.data.PageRequest;
import com.blazebit.persistence.deltaspike.data.Pageable;
import com.blazebit.persistence.deltaspike.data.rest.PageableConfiguration;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PageableConfigurationImpl implements PageableConfiguration {

    private static final String DEFAULT_OFFSET_PARAMETER = "offset";
    private static final String DEFAULT_PAGE_PARAMETER = "page";
    private static final String DEFAULT_SIZE_PARAMETER = "size";
    private static final String DEFAULT_SORT_PARAMETER = "sort";
    private static final String DEFAULT_PREFIX = "";
    private static final int DEFAULT_MAX_PAGE_SIZE = 2000;
    private static final Pageable DEFAULT_PAGE_REQUEST = new PageRequest(0, 20);

    private Pageable fallbackPageable = DEFAULT_PAGE_REQUEST;
    private String offsetParameterName = DEFAULT_OFFSET_PARAMETER;
    private String pageParameterName = DEFAULT_PAGE_PARAMETER;
    private String sizeParameterName = DEFAULT_SIZE_PARAMETER;
    private String sortParameterName = DEFAULT_SORT_PARAMETER;
    private String prefix = DEFAULT_PREFIX;
    private int maxPageSize = DEFAULT_MAX_PAGE_SIZE;
    private boolean oneIndexedParameters = false;

    public PageableConfigurationImpl() {
    }

    public PageableConfigurationImpl(PageableConfiguration original) {
        this.fallbackPageable = original.getFallbackPageable();
        this.offsetParameterName = original.getOffsetParameterName();
        this.pageParameterName = original.getPageParameterName();
        this.sizeParameterName = original.getSizeParameterName();
        this.sortParameterName = original.getSortParameterName();
        this.prefix = original.getPrefix();
        this.maxPageSize = original.getMaxPageSize();
        this.oneIndexedParameters = original.isOneIndexedParameters();
    }

    @Override
    public Pageable getFallbackPageable() {
        return fallbackPageable;
    }

    @Override
    public void setFallbackPageable(Pageable fallbackPageable) {
        this.fallbackPageable = fallbackPageable;
    }

    @Override
    public String getOffsetParameterName() {
        return offsetParameterName;
    }

    @Override
    public void setOffsetParameterName(String offsetParameterName) {
        this.offsetParameterName = offsetParameterName;
    }

    @Override
    public String getPageParameterName() {
        return pageParameterName;
    }

    @Override
    public void setPageParameterName(String pageParameterName) {
        this.pageParameterName = pageParameterName;
    }

    @Override
    public String getSizeParameterName() {
        return sizeParameterName;
    }

    @Override
    public void setSizeParameterName(String sizeParameterName) {
        this.sizeParameterName = sizeParameterName;
    }

    @Override
    public String getSortParameterName() {
        return sortParameterName;
    }

    @Override
    public void setSortParameterName(String sortParameterName) {
        this.sortParameterName = sortParameterName;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public int getMaxPageSize() {
        return maxPageSize;
    }

    @Override
    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    @Override
    public boolean isOneIndexedParameters() {
        return oneIndexedParameters;
    }

    @Override
    public void setOneIndexedParameters(boolean oneIndexedParameters) {
        this.oneIndexedParameters = oneIndexedParameters;
    }
}
