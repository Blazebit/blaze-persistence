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
import com.blazebit.persistence.deltaspike.data.PageRequest;
import com.blazebit.persistence.deltaspike.data.Pageable;
import com.blazebit.persistence.deltaspike.data.Sort;
import com.blazebit.persistence.deltaspike.data.rest.KeysetConfig;
import com.blazebit.persistence.deltaspike.data.rest.KeysetPageableConfiguration;
import com.blazebit.persistence.deltaspike.data.rest.PageableConfiguration;
import com.blazebit.persistence.deltaspike.data.rest.PageableDefault;
import com.blazebit.persistence.deltaspike.data.rest.SortDefault;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@Provider
public class KeysetPageableParamConverterProvider implements ParamConverterProvider {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Context
    private UriInfo requestUriInfo;
    @Inject
    private PageableConfiguration pageableConfiguration;
    @Inject
    private KeysetPageableConfiguration keysetPageableConfiguration;

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type type, Annotation[] annotations) {
        if (Pageable.class.isAssignableFrom(rawType)) {
            KeysetConfig keysetConfig = null;
            PageableDefault pageableDefault = null;
            List<SortDefault> sortDefaults = new ArrayList<>();
            for (int i = 0; i < annotations.length; i++) {
                Class<? extends Annotation> annotationType = annotations[i].annotationType();
                if (annotationType == KeysetConfig.class) {
                    keysetConfig = (KeysetConfig) annotations[i];
                } else if (annotationType == PageableDefault.class) {
                    pageableDefault = (PageableDefault) annotations[i];
                } else if (annotationType == SortDefault.class) {
                    sortDefaults.add((SortDefault) annotations[i]);
                } else if (annotationType == SortDefault.SortDefaults.class) {
                    Collections.addAll(sortDefaults, ((SortDefault.SortDefaults) annotations[i]).value());
                }
            }

            Class<?> keysetDomainClass;
            PageableConfiguration pageableConfiguration;
            KeysetPageableConfiguration keysetPageableConfiguration;
            if (keysetConfig == null) {
                keysetDomainClass = null;
                pageableConfiguration = resolvePageableConfiguration(pageableDefault, sortDefaults);
                keysetPageableConfiguration = null;
            } else {
                keysetDomainClass = keysetConfig.keysetClass();
                if (keysetDomainClass == void.class) {
                    keysetDomainClass = keysetConfig.value();
                }

                if (keysetDomainClass == void.class) {
                    throw new IllegalStateException("Invalid keyset domain class configured for method! Should be an entity type!");
                }
                pageableConfiguration = keysetPageableConfiguration = resolveKeysetPageableConfiguration(keysetConfig, pageableDefault, sortDefaults);
            }

            return (ParamConverter<T>) new KeysetPageableParamConverter(keysetDomainClass, MAPPER, requestUriInfo, pageableConfiguration, keysetPageableConfiguration);
        }

        return null;
    }

    private KeysetPageableConfiguration resolveKeysetPageableConfiguration(KeysetConfig keysetConfig, PageableDefault pageableDefault, List<SortDefault> sortDefaults) {
        if (pageableDefault == null && sortDefaults.isEmpty()) {
            return keysetPageableConfiguration;
        }
        KeysetPageableConfigurationImpl keysetPageableConfiguration = new KeysetPageableConfigurationImpl(this.keysetPageableConfiguration);
        apply(keysetPageableConfiguration, pageableDefault, sortDefaults);
        if (keysetConfig != null) {
            if (!keysetConfig.highestName().isEmpty()) {
                keysetPageableConfiguration.setHighestParameterName(keysetConfig.highestName());
            }
            if (!keysetConfig.lowestName().isEmpty()) {
                keysetPageableConfiguration.setLowestParameterName(keysetConfig.lowestName());
            }
            if (!keysetConfig.previousOffsetName().isEmpty()) {
                keysetPageableConfiguration.setPreviousOffsetParameterName(keysetConfig.previousOffsetName());
            }
            if (!keysetConfig.previousPageName().isEmpty()) {
                keysetPageableConfiguration.setPreviousPageParameterName(keysetConfig.previousPageName());
            }
            if (!keysetConfig.previousPageSizeName().isEmpty()) {
                keysetPageableConfiguration.setPreviousSizeParameterName(keysetConfig.previousPageSizeName());
            }
        }
        keysetPageableConfiguration.setFallbackPageable(new KeysetPageRequest(null, keysetPageableConfiguration.getFallbackPageable()));
        return keysetPageableConfiguration;
    }

    private PageableConfiguration resolvePageableConfiguration(PageableDefault pageableDefault, List<SortDefault> sortDefaults) {
        if (pageableDefault == null && sortDefaults.isEmpty()) {
            return pageableConfiguration;
        }
        PageableConfigurationImpl pageableConfiguration = new PageableConfigurationImpl(this.pageableConfiguration);
        apply(pageableConfiguration, pageableDefault, sortDefaults);
        return keysetPageableConfiguration;
    }

    private void apply(PageableConfiguration pageableConfiguration, PageableDefault pageableDefault, List<SortDefault> sortDefaults) {
        int page = pageableConfiguration.getFallbackPageable().getPageNumber();
        int pageSize = pageableConfiguration.getFallbackPageable().getPageSize();
        Sort sort = pageableConfiguration.getFallbackPageable().getSort();
        if (pageableDefault != null) {
            page = pageableDefault.page();
            pageSize = pageableDefault.size();
            if (!pageableDefault.pageName().isEmpty()) {
                pageableConfiguration.setPageParameterName(pageableDefault.pageName());
            }
            if (!pageableDefault.pageSizeName().isEmpty()) {
                pageableConfiguration.setSizeParameterName(pageableDefault.pageSizeName());
            }
            if (!pageableDefault.sortName().isEmpty()) {
                pageableConfiguration.setSortParameterName(pageableDefault.sortName());
            }

            pageableConfiguration.setOneIndexedParameters(pageableDefault.oneIndexed());
            if (pageableDefault.sort().length != 0) {
                if (!sortDefaults.isEmpty()) {
                    throw new IllegalArgumentException("Annotating both, @PageableDefault with sort and @SortDefault isn't allowed!");
                }
                sort = new Sort(pageableDefault.direction(), pageableDefault.sort());
            }
        }
        if (!sortDefaults.isEmpty()) {
            List<Sort.Order> orders = new ArrayList<>(sortDefaults.size());
            for (int i = 0; i < sortDefaults.size(); i++) {
                SortDefault sortDefault = sortDefaults.get(i);
                String[] sortPaths = sortDefault.sort();
                for (int j = 0; j < sortPaths.length; j++) {
                    orders.add(new Sort.Order(sortDefault.direction(), sortPaths[j], sortDefault.nulls()));
                }
            }
            sort = new Sort(orders);
        }

        int offset;
        if (page == 0) {
            offset = 0;
        } else {
            offset = page * pageSize;
        }
        pageableConfiguration.setFallbackPageable(new PageRequest(sort, offset, pageSize));
    }

}
