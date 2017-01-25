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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.IdentifiableType;

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.ViewFilter;
import com.blazebit.persistence.view.ViewFilters;
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
    private final boolean updatable;
    private final boolean partiallyUpdatable;
    private final MethodAttribute<? super X, ?> idAttribute;
    private final Map<String, ViewFilterMapping> viewFilters;

    public ViewTypeImpl(Class<? extends X> clazz, Set<Class<?>> entityViews, EntityMetamodel metamodel, ExpressionFactory expressionFactory, Set<String> errors) {
        super(clazz, getEntityClass(clazz, metamodel, errors), entityViews, metamodel, expressionFactory, errors);

        EntityView entityViewAnnot = AnnotationUtils.findAnnotation(clazz, EntityView.class);

        if (entityViewAnnot.name().isEmpty()) {
            this.name = clazz.getSimpleName();
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
                errors.add("Illegal occurrences of @ViewFilter and @ViewFilters on the class '" + javaType.getName() + "'!");
            } else {
                addFilterMapping(filterMapping, viewFilters, errors);
            }
        } else if (filtersMapping != null) {
            for (ViewFilter f : filtersMapping.value()) {
                addFilterMapping(f, viewFilters, errors);
            }
        }

        this.viewFilters = Collections.unmodifiableMap(viewFilters);
        
        MethodAttribute<? super X, ?> foundIdAttribute = null;
        
        for (AbstractMethodAttribute<? super X, ?> attribute : attributes.values()) {
            if (attribute.isId()) {
                if (foundIdAttribute != null) {
                    errors.add("Illegal occurrence of multiple id attributes ['" + foundIdAttribute.getName() + "', '" + attribute.getName() + "'] in entity view '" + javaType.getName() + "'!");
                } else {
                    foundIdAttribute = attribute;
                }
            }
        }
        
        if (foundIdAttribute == null) {
            errors.add("No id attribute was defined for entity view '" + javaType.getName() + "' although it is needed!");
        }
        
        if (updatable) {
            if (foundIdAttribute.isUpdatable()) {
                errors.add("Id attribute in entity view '" + javaType.getName() + "' is updatable which is not allowed!");
            }
        }

        this.idAttribute = foundIdAttribute;
    }
    
    private static Class<?> getEntityClass(Class<?> clazz, EntityMetamodel metamodel, Set<String> errors) {
        EntityView entityViewAnnot = AnnotationUtils.findAnnotation(clazz, EntityView.class);

        if (entityViewAnnot == null) {
            errors.add("Could not find any EntityView annotation for the class '" + clazz.getName() + "'");
            return null;
        }

        Class<?> entityClass = entityViewAnnot.value();

        if (!(metamodel.getManagedType(entityClass) instanceof IdentifiableType<?>)) {
            errors.add("The class which is referenced by the EntityView annotation of the class '" + clazz.getName() + "' is not an identifiable type!");
            return null;
        }
        
        return entityClass;
    }

    private void addFilterMapping(ViewFilter filterMapping,Map<String, ViewFilterMapping> viewFilters, Set<String> errors) {
        String filterName = filterMapping.name();
        boolean errorOccurred = false;

        if (filterName.isEmpty()) {
            filterName = name;
            
            if (viewFilters.containsKey(filterName)) {
                errorOccurred = true;
                errors.add("Illegal duplicate filter name mapping '" + filterName + "' at the class '" + javaType.getName() + "'!");
            }
        }

        if (filterName != null && filterName.isEmpty()) {
            errors.add("Illegal empty name for the filter mapping at the class '" + this.getJavaType().getName() + "' with filter class '"
                    + filterMapping.value().getName() + "'!");
        }

        if (!errorOccurred) {
            ViewFilterMapping viewFilterMapping = new ViewFilterMappingImpl(this, filterName, filterMapping.value());
            viewFilters.put(viewFilterMapping.getName(), viewFilterMapping);
        }
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
