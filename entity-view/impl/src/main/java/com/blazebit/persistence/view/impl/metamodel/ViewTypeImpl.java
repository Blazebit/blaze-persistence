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

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.persistence.view.LockMode;
import com.blazebit.persistence.view.ViewFilter;
import com.blazebit.persistence.view.ViewFilters;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewFilterMapping;

import javax.persistence.metamodel.ManagedType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ViewTypeImpl<X> extends ManagedViewTypeImpl<X> implements ViewTypeImplementor<X> {

    private final String name;
    private final String lockOwner;
    private final MethodAttribute<? super X, ?> idAttribute;
    private final MethodAttribute<? super X, ?> versionAttribute;
    private final Map<String, ViewFilterMapping> viewFilters;

    public ViewTypeImpl(ViewMapping viewMapping, ManagedType<?> managedType, MetamodelBuildingContext context) {
        super(viewMapping, managedType, context);

        String name = viewMapping.getName();

        if (name == null || name.isEmpty()) {
            this.name = getJavaType().getSimpleName();
        } else {
            this.name = name;
        }

        Map<String, ViewFilterMapping> viewFilters = new HashMap<String, ViewFilterMapping>();
        
        ViewFilter filterMapping = AnnotationUtils.findAnnotation(getJavaType(), ViewFilter.class);
        ViewFilters filtersMapping = AnnotationUtils.findAnnotation(getJavaType(), ViewFilters.class);
        
        if (filterMapping != null) {
            if (filtersMapping != null) {
                context.addError("Illegal occurrences of @ViewFilter and @ViewFilters on the class '" + getJavaType().getName() + "'!");
            } else {
                addFilterMapping(filterMapping, viewFilters, context);
            }
        } else if (filtersMapping != null) {
            for (ViewFilter f : filtersMapping.value()) {
                addFilterMapping(f, viewFilters, context);
            }
        }

        this.viewFilters = Collections.unmodifiableMap(viewFilters);
        this.idAttribute = viewMapping.getIdAttribute().getMethodAttribute(this, -1, -1, context);

        if (getLockMode() != LockMode.NONE) {
            if (viewMapping.getVersionAttribute() != null) {
                this.versionAttribute = viewMapping.getVersionAttribute().getMethodAttribute(this, -1, -1, context);
            } else {
                this.versionAttribute = null;
            }
            // TODO: validate lock owner path is valid and target has a version if optimistic
            // Also verify that lock owner isn't set when we have a version attribute?
            this.lockOwner = viewMapping.getLockOwner();
        } else {
            this.versionAttribute = null;
            this.lockOwner = null;
            if (viewMapping.getVersionAttribute() != null) {
                context.addError("Invalid version attribute mapping defined for managed view type '" + getJavaType().getName() + "'!");
            }
            if (viewMapping.getLockOwner() != null) {
                context.addError("Invalid lock owner mapping defined for managed view type '" + getJavaType().getName() + "'!");
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
                context.addError("Illegal duplicate filter name mapping '" + filterName + "' at the class '" + getJavaType().getName() + "'!");
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
    public MethodAttribute<? super X, ?> getIdAttribute() {
        return idAttribute;
    }

    @Override
    public MethodAttribute<? super X, ?> getVersionAttribute() {
        return versionAttribute;
    }

    @Override
    public String getLockOwner() {
        return lockOwner;
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
