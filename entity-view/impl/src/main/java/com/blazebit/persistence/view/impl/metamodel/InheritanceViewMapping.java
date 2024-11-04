/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
// Make the order of the InheritanceViewMappings deterministic, otherwise clustering won't work
public class InheritanceViewMapping implements Comparable<InheritanceViewMapping> {

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
    public int compareTo(InheritanceViewMapping o) {
        int cmp = Integer.compare(inheritanceSubtypeMappings.size(), o.inheritanceSubtypeMappings.size());
        if (cmp != 0) {
            return cmp;
        }
        Iterator<Map.Entry<ViewMapping, String>> firstIter = inheritanceSubtypeMappings.entrySet().iterator();
        Iterator<Map.Entry<ViewMapping, String>> secondIter = o.inheritanceSubtypeMappings.entrySet().iterator();
        while (firstIter.hasNext()) {
            Map.Entry<ViewMapping, String> first = firstIter.next();
            Map.Entry<ViewMapping, String> second = secondIter.next();

            cmp = first.getKey().getEntityViewClass().getName().compareTo(
                    second.getKey().getEntityViewClass().getName()
            );
            if (cmp != 0) {
                return cmp;
            }

            if (first.getValue() == null) {
                if (second.getValue() != null) {
                    return -1;
                }
            } else if (second.getValue() == null) {
                return 1;
            } else {
                cmp = first.getValue().compareTo(second.getValue());
                if (cmp != 0) {
                    return cmp;
                }
            }
        }

        return 0;
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
