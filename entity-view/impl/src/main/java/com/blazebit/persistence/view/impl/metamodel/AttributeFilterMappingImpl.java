/*
 * Copyright 2014 - 2021 Blazebit.
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
