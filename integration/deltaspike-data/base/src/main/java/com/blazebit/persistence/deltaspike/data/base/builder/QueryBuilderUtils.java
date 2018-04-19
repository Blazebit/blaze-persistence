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

package com.blazebit.persistence.deltaspike.data.base.builder;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.deltaspike.data.KeysetPageable;
import com.blazebit.persistence.deltaspike.data.Pageable;
import com.blazebit.persistence.deltaspike.data.Sort;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.Sorters;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class QueryBuilderUtils {

    private QueryBuilderUtils() {
    }

    public static <V> FullQueryBuilder<?, ?> getFullQueryBuilder(CriteriaBuilder<?> cb, Pageable pageable, EntityViewManager evm, Class<V> entityViewClass, boolean keysetExtraction) {
        if (pageable != null) {
            QueryBuilderUtils.applySort(pageable.getSort(), cb);
        }

        FullQueryBuilder<?, ?> fullCb;
        if (entityViewClass == null) {
            if (pageable == null) {
                fullCb = cb;
            } else {
                PaginatedCriteriaBuilder<?> paginatedCriteriaBuilder;
                if (pageable instanceof KeysetPageable) {
                    paginatedCriteriaBuilder = cb.page(((KeysetPageable) pageable).getKeysetPage(), pageable.getOffset(), pageable.getPageSize());
                } else {
                    paginatedCriteriaBuilder = cb.page(pageable.getOffset(), pageable.getPageSize());
                }
                if (keysetExtraction) {
                    paginatedCriteriaBuilder.withKeysetExtraction(true);
                }
                fullCb = paginatedCriteriaBuilder;
            }
        } else {
            if (pageable == null) {
                fullCb = evm.applySetting(EntityViewSetting.create(entityViewClass), cb);
            } else {
                EntityViewSetting<V, PaginatedCriteriaBuilder<V>> setting = EntityViewSetting.create(entityViewClass, pageable.getOffset(), pageable.getPageSize());
                if (pageable instanceof KeysetPageable) {
                    setting.withKeysetPage(((KeysetPageable) pageable).getKeysetPage());
                }
                PaginatedCriteriaBuilder<?> paginatedCriteriaBuilder = (PaginatedCriteriaBuilder) evm.applySetting(setting, cb);
                if (keysetExtraction) {
                    paginatedCriteriaBuilder.withKeysetExtraction(true);
                }
                fullCb = paginatedCriteriaBuilder;
            }
        }
        return fullCb;
    }

    public static void applySort(Sort sort, QueryBuilder<?, ?> cb) {
        if (sort != null) {
            for (Sort.Order order : sort) {
                if (order.getNullHandling() == Sort.NullHandling.NATIVE) {
                    if (order.isAscending()) {
                        cb.orderByAsc(order.getPath());
                    } else {
                        cb.orderByDesc(order.getPath());
                    }
                } else {
                    if (order.isAscending()) {
                        cb.orderByAsc(order.getPath(), order.getNullHandling() == Sort.NullHandling.NULLS_FIRST);
                    } else {
                        cb.orderByDesc(order.getPath(), order.getNullHandling() == Sort.NullHandling.NULLS_FIRST);
                    }
                }
            }
        }
    }

    public static void applySort(Sort sort, EntityViewSetting<?, ?> setting) {
        if (sort != null) {
            for (Sort.Order order : sort) {
                if (order.getNullHandling() == Sort.NullHandling.NATIVE) {
                    if (order.isAscending()) {
                        setting.addAttributeSorter(order.getPath(), Sorters.ascending());
                    } else {
                        setting.addAttributeSorter(order.getPath(), Sorters.descending());
                    }
                } else {
                    if (order.isAscending()) {
                        setting.addAttributeSorter(order.getPath(), Sorters.ascending(order.getNullHandling() == Sort.NullHandling.NULLS_FIRST));
                    } else {
                        setting.addAttributeSorter(order.getPath(), Sorters.descending(order.getNullHandling() == Sort.NullHandling.NULLS_FIRST));
                    }
                }
            }
        }
    }
}
