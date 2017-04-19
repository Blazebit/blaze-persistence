/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.view.impl.metamodel;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class InheritanceViewMapping {

    private final Map<ViewMapping, String> inheritanceSubtypeMappings;

    public InheritanceViewMapping(ViewMapping baseType, Set<ViewMapping> inheritanceSubtypes) {
        Map<ViewMapping, String> sortedMap = createSortedMap();
        sortedMap.put(baseType, null);
        for (ViewMapping subtypeMapping : inheritanceSubtypes) {
            sortedMap.put(subtypeMapping, null);
        }
        this.inheritanceSubtypeMappings = sortedMap;
    }

    public InheritanceViewMapping(Map<ViewMapping, String> inheritanceSubtypeMappings) {
        Map<ViewMapping, String> sortedMap = createSortedMap();
        sortedMap.putAll(inheritanceSubtypeMappings);
        this.inheritanceSubtypeMappings = sortedMap;
    }

    private Map<ViewMapping, String> createSortedMap() {
        // Order from abstract to concrete, left to right
        return new TreeMap<>(new Comparator<ViewMapping>() {
            @Override
            public int compare(ViewMapping o1, ViewMapping o2) {
                if (o1.getEntityViewClass() == o2.getEntityViewClass()) {
                    return 0;
                }
                if (o1.getEntityViewClass().isAssignableFrom(o2.getEntityViewClass())) {
                    return -1;
                }
                if (o2.getEntityViewClass().isAssignableFrom(o1.getEntityViewClass())) {
                    return 1;
                }

                return o1.getEntityViewClass().getName().compareTo(o2.getEntityViewClass().getName());
            }
        });
    }

    public Map<ViewMapping, String> getInheritanceSubtypeMappings() {
        return inheritanceSubtypeMappings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InheritanceViewMapping)) {
            return false;
        }

        InheritanceViewMapping that = (InheritanceViewMapping) o;

        return getInheritanceSubtypeMappings() != null ? getInheritanceSubtypeMappings().equals(that.getInheritanceSubtypeMappings()) : that.getInheritanceSubtypeMappings() == null;
    }

    @Override
    public int hashCode() {
        return getInheritanceSubtypeMappings() != null ? getInheritanceSubtypeMappings().hashCode() : 0;
    }
}
