/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.ViewFilterProvider;
import com.blazebit.persistence.view.metamodel.ViewFilterMapping;
import com.blazebit.persistence.view.metamodel.ViewType;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ViewFilterMappingImpl implements ViewFilterMapping {

    private final ViewType<?> declaringType;
    private final String name;
    private final Class<? extends ViewFilterProvider> filterClass;

    public ViewFilterMappingImpl(ViewType<?> declaringType, String name, Class<? extends ViewFilterProvider> filterClass) {
        this.declaringType = declaringType;
        this.name = name;
        this.filterClass = filterClass;

        if (name == null) {
            throw new NullPointerException("name");
        }
    }

    @Override
    public ViewType<?> getDeclaringType() {
        return declaringType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<? extends ViewFilterProvider> getFilterClass() {
        return filterClass;
    }

    @Override
    public boolean isViewFilter() {
        return false;
    }

}
