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

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.ViewFilter;
import com.blazebit.persistence.view.ViewFilters;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewFilterMapping;
import com.blazebit.persistence.view.metamodel.ViewType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class ViewTypeImpl<X> extends ManagedViewTypeImpl<X> implements ViewType<X> {

    private final String name;
    private final boolean updatable;
    private final boolean partiallyUpdatable;
    private final MethodAttribute<? super X, ?> idAttribute;
    private final Map<String, ViewFilterMapping> viewFilters;

    public ViewTypeImpl(ViewMapping viewMapping, MetamodelBuildingContext context) {
        super(viewMapping, context);

        EntityView entityViewAnnot = viewMapping.getMapping();

        if (entityViewAnnot.name().isEmpty()) {
            this.name = javaType.getSimpleName();
        } else {
            this.name = entityViewAnnot.name();
        }

        UpdatableEntityView updatableEntityView = AnnotationUtils.findAnnotation(javaType, UpdatableEntityView.class);
        if (updatableEntityView != null) {
            this.updatable = true;
            this.partiallyUpdatable = updatableEntityView.partial();
        } else {
            this.updatable = false;
            this.partiallyUpdatable = false;
        }

        Map<String, ViewFilterMapping> viewFilters = new HashMap<String, ViewFilterMapping>();
        
        ViewFilter filterMapping = AnnotationUtils.findAnnotation(javaType, ViewFilter.class);
        ViewFilters filtersMapping = AnnotationUtils.findAnnotation(javaType, ViewFilters.class);
        
        if (filterMapping != null) {
            if (filtersMapping != null) {
                context.addError("Illegal occurrences of @ViewFilter and @ViewFilters on the class '" + javaType.getName() + "'!");
            } else {
                addFilterMapping(filterMapping, viewFilters, context);
            }
        } else if (filtersMapping != null) {
            for (ViewFilter f : filtersMapping.value()) {
                addFilterMapping(f, viewFilters, context);
            }
        }

        this.viewFilters = Collections.unmodifiableMap(viewFilters);
        this.idAttribute = viewMapping.getIdAttribute().getMethodAttribute(this);

        if (updatable) {
            if (idAttribute.isUpdatable()) {
                context.addError("Id attribute in entity view '" + javaType.getName() + "' is updatable which is not allowed!");
            }
        }
    }

    @Override
    protected boolean hasId() {
        return true;
    }

    private void addFilterMapping(ViewFilter filterMapping, Map<String, ViewFilterMapping> viewFilters, MetamodelBuildingContext context) {
        String filterName = filterMapping.name();
        boolean errorOccurred = false;

        if (filterName.isEmpty()) {
            filterName = name;
            
            if (viewFilters.containsKey(filterName)) {
                errorOccurred = true;
                context.addError("Illegal duplicate filter name mapping '" + filterName + "' at the class '" + javaType.getName() + "'!");
            }
        }

        if (filterName != null && filterName.isEmpty()) {
            context.addError("Illegal empty name for the filter mapping at the class '" + this.getJavaType().getName() + "' with filter class '"
                    + filterMapping.value().getName() + "'!");
        }

        if (!errorOccurred) {
            ViewFilterMapping viewFilterMapping = new ViewFilterMappingImpl(this, filterName, filterMapping.value());
            viewFilters.put(viewFilterMapping.getName(), viewFilterMapping);
        }
    }

    @Override
    public MappingType getMappingType() {
        return MappingType.VIEW;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isUpdatable() {
        return updatable;
    }

    @Override
    public boolean isPartiallyUpdatable() {
        return partiallyUpdatable;
    }

    @Override
    public MethodAttribute<? super X, ?> getIdAttribute() {
        return idAttribute;
    }

    @Override
    public ViewFilterMapping getViewFilter(String filterName) {
        return viewFilters.get(filterName);
    }

    @Override
    public Set<ViewFilterMapping> getViewFilters() {
        return new SetView<ViewFilterMapping>(viewFilters.values());
    }

}
