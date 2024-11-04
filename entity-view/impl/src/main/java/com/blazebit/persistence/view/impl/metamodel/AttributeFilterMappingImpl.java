/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.AttributeFilterProvider;
import com.blazebit.persistence.view.metamodel.AttributeFilterMapping;
import com.blazebit.persistence.view.metamodel.MethodAttribute;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class AttributeFilterMappingImpl<X, FilterValue> implements AttributeFilterMapping<X, FilterValue> {

    private final MethodAttribute<X, ?> declaringAttribute;
    private final String name;
    private final Class<? extends AttributeFilterProvider<FilterValue>> filterClass;

    public AttributeFilterMappingImpl(MethodAttribute<X, ?> declaringAttribute, String name, Class<? extends AttributeFilterProvider<FilterValue>> filterClass) {
        this.declaringAttribute = declaringAttribute;
        this.name = name;
        this.filterClass = filterClass;

        if (name == null) {
            throw new NullPointerException("name");
        }
    }

    @Override
    public MethodAttribute<X, ?> getDeclaringAttribute() {
        return declaringAttribute;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<? extends AttributeFilterProvider<FilterValue>> getFilterClass() {
        return filterClass;
    }

    @Override
    public boolean isViewFilter() {
        return false;
    }

}
