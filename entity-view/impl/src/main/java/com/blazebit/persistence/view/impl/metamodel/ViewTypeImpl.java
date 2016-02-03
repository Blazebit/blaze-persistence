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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdateableEntityView;
import com.blazebit.persistence.view.ViewFilter;
import com.blazebit.persistence.view.ViewFilters;
import com.blazebit.persistence.view.metamodel.FilterMapping;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewFilterMapping;
import com.blazebit.persistence.view.metamodel.ViewType;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class ViewTypeImpl<X> extends ManagedViewTypeImpl<X> implements ViewType<X> {

    private final String name;
    private final boolean updateable;
    private final boolean partiallyUpdateable;
    private final MethodAttribute<? super X, ?> idAttribute;
    private final Map<String, ViewFilterMapping> viewFilters;

    
    private static Class<?> getEntityClass(Class<?> clazz) {
        EntityView entityViewAnnot = AnnotationUtils.findAnnotation(clazz, EntityView.class);

        if (entityViewAnnot == null) {
            throw new IllegalArgumentException("Could not find any EntityView annotation for the class '" + clazz.getName() + "'");
        }

        return entityViewAnnot.value();
    }
    public ViewTypeImpl(Class<? extends X> clazz, Set<Class<?>> entityViews) {
        super(clazz, getEntityClass(clazz), entityViews);

        EntityView entityViewAnnot = AnnotationUtils.findAnnotation(clazz, EntityView.class);

        if (entityViewAnnot.name().isEmpty()) {
            this.name = clazz.getSimpleName();
        } else {
            this.name = entityViewAnnot.name();
        }

        // TODO: updateable entity views have restrictions on the mappings
        UpdateableEntityView updateableEntityView = AnnotationUtils.findAnnotation(javaType, UpdateableEntityView.class);
        if (updateableEntityView != null) {
        	this.updateable = true;
        	this.partiallyUpdateable = updateableEntityView.partial();
        } else {
        	this.updateable = false;
        	this.partiallyUpdateable = false;
        }
        
        this.viewFilters = new HashMap<String, ViewFilterMapping>();
        
        ViewFilter filterMapping = AnnotationUtils.findAnnotation(javaType, ViewFilter.class);
        ViewFilters filtersMapping = AnnotationUtils.findAnnotation(javaType, ViewFilters.class);
        
        if (filterMapping != null) {
            if (filtersMapping != null) {
                throw new IllegalArgumentException("Illegal occurrences of @ViewFilter and @ViewFilters on the class '" + javaType.getName() + "'!");
            }
            
            addFilterMapping(filterMapping);
        } else if (filtersMapping != null) {
            for (ViewFilter f : filtersMapping.value()) {
                addFilterMapping(f);
            }
        }
        
        MethodAttribute<? super X, ?> foundIdAttribute = null;
        
        for (AbstractMethodAttribute<? super X, ?> attribute : attributes.values()) {
            if (attribute.isId()) {
                if (foundIdAttribute != null) {
                    throw new IllegalArgumentException("Illegal occurrence of multiple id attributes ['" + foundIdAttribute.getName() + "', '" + attribute.getName() + "'] in entity view '" + javaType.getName() + "'!");
                } else {
                    foundIdAttribute = attribute;
                }
            }
            
            // TODO: remove this as soon as we have support for collection updates
            if (attribute.isCollection() && updateable && attribute.isUpdateable()) {
            	throw new IllegalArgumentException("Collection updates are not yet implemented! Please remove the setter for the attribute [" + attribute.getName() + "] from [" + javaType.getName() + "]");
            }
        }
        
        if (foundIdAttribute == null) {
            throw new IllegalArgumentException("No id attribute was defined for entity view '" + javaType.getName() + "' although it is needed!");
        }
        
        if (updateable) {
	        if (foundIdAttribute.isUpdateable()) {
	        	throw new IllegalArgumentException("Id attribute in entity view '" + javaType.getName() + "' is updateable which is not allowed!");
	        }
        }

        this.idAttribute = foundIdAttribute;
    }

    private void addFilterMapping(ViewFilter filterMapping) {
        String filterName = filterMapping.name();
        
        if (filterName.isEmpty()) {
            filterName = name;
            
            if (viewFilters.containsKey(filterName)) {
                throw new IllegalArgumentException("Illegal duplicate filter name mapping '" + filterName + "' at the class '" + javaType.getName() + "'!");
            } else if (attributeFilters.containsKey(filterName)) {
                throw new IllegalArgumentException("Illegal duplicate filter name mapping '" + filterName + "' at attribute '" + attributeFilters.get(filterName).getDeclaringAttribute().getName() 
                                                   + "' of the class '" + javaType.getName() + "'! Already defined on class '" + javaType.getName() + "'!");
            }
        }
        
        ViewFilterMapping viewFilterMapping = new ViewFilterMappingImpl(this, filterName, filterMapping.value());
        viewFilters.put(viewFilterMapping.getName(), viewFilterMapping);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isUpdateable() {
        return updateable;
    }

    @Override
	public boolean isPartiallyUpdateable() {
		return partiallyUpdateable;
	}

    @Override
    public MethodAttribute<? super X, ?> getIdAttribute() {
        return idAttribute;
    }

    @Override
    public FilterMapping<?> getFilter(String filterName) {
        FilterMapping<?> filterMapping = attributeFilters.get(filterName);
        return filterMapping != null ? filterMapping : viewFilters.get(filterName);
    }

    @Override
    public Set<FilterMapping<?>> getFilters() {
        Set<FilterMapping<?>> filters = new HashSet<FilterMapping<?>>(attributeFilters.size() + viewFilters.size());
        filters.addAll(viewFilters.values());
        filters.addAll(attributeFilters.values());
        return filters;
    }

    @Override
    public ViewFilterMapping getViewFilter(String filterName) {
        return viewFilters.get(filterName);
    }

    @Override
    public Set<ViewFilterMapping> getViewFilters() {
        return new HashSet<ViewFilterMapping>(viewFilters.values());
    }

}
