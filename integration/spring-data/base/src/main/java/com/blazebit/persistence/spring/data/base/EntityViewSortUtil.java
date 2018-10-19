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

package com.blazebit.persistence.spring.data.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.NullHandling;
import org.springframework.data.domain.Sort.Order;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.Sorter;
import com.blazebit.persistence.view.Sorters;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.Type;

/**
 * Utility methods to handle entity view sorting.
 * 
 * @author Giovanni Lovato
 * @since 1.3.0
 */

public final class EntityViewSortUtil {

    private EntityViewSortUtil() {
    }

    /**
     * Checks if the given {@link Order} refers to a property of {@code entityViewClass}.
     * 
     * @param evm the entity view manager
     * @param entityViewClass the entity view class
     * @param order the order
     * @return true, if the given order refers to a view property
     */
    public static boolean isEntityViewSorting(EntityViewManager evm, Class<?> entityViewClass, Order order) {
        String property = order.getProperty();
        ManagedViewType<?> viewType = evm.getMetamodel().view(entityViewClass);
        for (String path : property.split("\\.")) {
            MethodAttribute<?, ?> attribute = viewType.getAttribute(path);
            if (attribute == null) {
                return false;
            } else {
                Type<?> type;
                if (attribute instanceof SingularAttribute) {
                    type = ((SingularAttribute<?, ?>) attribute).getType();
                } else {
                    type = ((PluralAttribute<?, ?, ?>) attribute).getElementType();
                }
                if (type instanceof ManagedViewType) {
                    // It's a view type, continue descending
                    viewType = (ManagedViewType<?>) type;
                } else {
                    // It's a basic type, sort by that
                    return true;
                }
            }
        }
        return viewType != null;
    }

    /**
     * Process a {@link Sort} instance applying entity-view related sort orders to {@code setting} and
     * returning a new {@link Sort} instance with only entity related sort orders.
     * 
     * @param evm the entity view manager
     * @param setting the entity view setting
     * @param sort the sort instance
     * @return a new {@link Sort} with only entity related sort orders
     */
    public static <T> Sort processEntityViewSortOrders(EntityViewManager evm, EntityViewSetting<T, ?> setting, Sort sort) {
        Class<T> entityViewClass = setting.getEntityViewClass();
        List<Order> orders = new ArrayList<>();
        Map<String, Sorter> sorters = new HashMap<>();
        for (Order order : sort) {
            if (isEntityViewSorting(evm, entityViewClass, order)) {
                boolean nullsFirst = order.getNullHandling().equals(NullHandling.NULLS_FIRST);
                Sorter sorter = order.isAscending() ? Sorters.ascending(nullsFirst) : Sorters.descending(nullsFirst);
                String property = order.getProperty();
                sorters.putIfAbsent(property, sorter);
            } else {
                orders.add(order);
            }
        }
        setting.addAttributeSorters(sorters);
        return orders.isEmpty() ? null : new Sort(orders);
    }

    /**
     * Returns a new {@link Sort} instance with only entity related sort orders.
     *
     * @param evm the entity view manager
     * @param entityViewClass the entity view class
     * @param sort the sort instance
     * @return a new {@link Sort} with only entity related sort orders
     */
    public static Sort removeEntityViewSortOrders(EntityViewManager evm, Class<?> entityViewClass, Sort sort) {
        List<Order> orders = new ArrayList<>();
        for (Order order : sort) {
            if (!isEntityViewSorting(evm, entityViewClass, order)) {
                orders.add(order);
            }
        }
        return orders.isEmpty() ? null : new Sort(orders);
    }

}
