/*
 * Copyright 2014 - 2020 Blazebit.
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

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import org.springframework.data.domain.Sort;

/**
 * Utility methods to handle entity view sorting.
 * 
 * @author Moritz Becker
 * @author Giovanni Lovato
 * @since 1.4.0
 */

public final class EntityViewSortUtil {

    private EntityViewSortUtil() {
    }

    /**
     * Resolves the deterministic select item alias for an entity view attribute.
     *
     * @param viewType entity view type
     * @param attributePath the absolute attribute path based on the {@code entityViewClass} for which the select alias should be resolved
     * @return the select item alias for the (nested) entity view attribute targeted by {@code attributePath} or {@code null}
     * if the {@code attributePath} cannot be resolved
     */
    private static String resolveViewAttributeSelectAlias(ManagedViewType<?> viewType, String attributePath) {
        StringBuilder aliasBuilder = new StringBuilder(viewType.getJavaType().getSimpleName());
        for (String pathElement : attributePath.split("\\.")) {
            if (viewType == null) {
                return null;
            } else {
                MethodAttribute<?, ?> attribute = viewType.getAttribute(pathElement);
                if (attribute == null) {
                    return null;
                } else {
                    aliasBuilder.append('_').append(pathElement);
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
                        // It's a basic type, so we cannot got further
                        viewType = null;
                    }
                }
            }
        }
        return aliasBuilder.toString();
    }

    public static void applySort(EntityViewManager evm, Class<?> entityViewClass, FullQueryBuilder<?, ?> cb, Sort sort) {
        ManagedViewType<?> viewType = evm.getMetamodel().managedViewOrError(entityViewClass);
        for (Sort.Order order : sort) {
            String entityViewAttributeAlias;
            if ((entityViewAttributeAlias = EntityViewSortUtil.resolveViewAttributeSelectAlias(viewType, order.getProperty())) == null) {
                cb.orderBy(order.getProperty(), order.isAscending(), order.getNullHandling() == Sort.NullHandling.NULLS_FIRST);
            } else {
                cb.orderBy(entityViewAttributeAlias, order.isAscending(), order.getNullHandling() == Sort.NullHandling.NULLS_FIRST);
            }
        }
    }
}
