/*
 * Copyright 2014 Blazebit.
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

import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author cpbec
 */
public class ViewMetamodelImpl implements ViewMetamodel {
    
    private final Map<Class<?>, ViewType<?>> views;

    public ViewMetamodelImpl(Set<Class<?>> entityViews) {
        this.views = new HashMap<Class<?>, ViewType<?>>(entityViews.size());
        
        for (Class<?> entityViewClass : entityViews) {
            views.put(entityViewClass, getViewType(entityViewClass));
        }
    }

    @Override
    public <X> ViewType<X> view(Class<X> clazz) {
        return (ViewType<X>) views.get(clazz);
    }

    @Override
    public Set<ViewType<?>> getViews() {
        return new HashSet<ViewType<?>>(views.values());
    }

    private ViewType<?> getViewType(Class<?> entityViewClass) {
        return new ViewTypeImpl<Object>(entityViewClass);
    }
    
}
