/*
 * Copyright 2014 - 2016 Blazebit.
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

import com.blazebit.persistence.view.ViewFilterProvider;
import com.blazebit.persistence.view.metamodel.ViewFilterMapping;
import com.blazebit.persistence.view.metamodel.ViewType;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class ViewFilterMappingImpl implements ViewFilterMapping {

    private final ViewType<?> declaringType;
    private final String name;
    private final Class<? extends ViewFilterProvider> filterClass;

    public ViewFilterMappingImpl(ViewType<?> declaringType, String name, Class<? extends ViewFilterProvider> filterClass, Set<String> errors) {
        this.declaringType = declaringType;
        this.name = name;
        this.filterClass = filterClass;

        if (name == null) {
            throw new NullPointerException("name");
        }
        if (name.isEmpty()) {
            errors.add("Illegal empty name for the filter mapping at the class '" + declaringType.getJavaType().getName() + "' with filter class '"
                + filterClass.getName() + "'!");
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
