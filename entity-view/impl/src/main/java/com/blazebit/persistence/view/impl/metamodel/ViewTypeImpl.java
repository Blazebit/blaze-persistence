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

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.MappingSingular;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.ViewFilter;
import com.blazebit.persistence.view.ViewFilters;
import com.blazebit.persistence.view.metamodel.AttributeFilterMapping;
import com.blazebit.persistence.view.metamodel.FilterMapping;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewFilterMapping;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.reflection.ReflectionUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class ViewTypeImpl<X> implements ViewType<X> {

    private final Class<X> javaType;
    private final String name;
    private final Class<?> entityClass;
    private final MethodAttribute<? super X, ?> idAttribute;
    private final Map<String, MethodAttribute<? super X, ?>> attributes;
    private final Map<ParametersKey, MappingConstructor<X>> constructors;
    private final Map<String, MappingConstructor<X>> constructorIndex;
    private final Map<String, AttributeFilterMapping> attributeFilters;
    private final Map<String, ViewFilterMapping> viewFilters;

    public ViewTypeImpl(Class<? extends X> clazz, Set<Class<?>> entityViews) {
        this.javaType = (Class<X>) clazz;

        if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
            throw new IllegalArgumentException("Only interfaces or abstract classes are allowed as entity views. '" + clazz.getName() + "' is neither of those.");
        }

        EntityView entityViewAnnot = AnnotationUtils.findAnnotation(clazz, EntityView.class);

        if (entityViewAnnot == null) {
            throw new IllegalArgumentException("Could not find any EntityView annotation for the class '" + clazz.getName() + "'");
        }

        if (entityViewAnnot.name().isEmpty()) {
            this.name = clazz.getSimpleName();
        } else {
            this.name = entityViewAnnot.name();
        }

        this.entityClass = entityViewAnnot.value();
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
        
        // We use a tree map to get a deterministic attribute order
        this.attributes = new TreeMap<String, MethodAttribute<? super X, ?>>();
        this.attributeFilters = new HashMap<String, AttributeFilterMapping>();
        
        MethodAttribute<? super X, ?> foundIdAttribute = null;
        
        for (Method method : clazz.getMethods()) {
            String attributeName = AbstractMethodAttribute.validate(this, method);

            if (attributeName != null && !attributes.containsKey(attributeName)) {
                AbstractMethodAttribute<? super X, ?> attribute = createMethodAttribute(this, method, entityViews);
                if (attribute != null) {
                    if (attribute.isId()) {
                        if (foundIdAttribute != null) {
                            throw new IllegalArgumentException("Illegal occurrence of multiple id attributes ['" + foundIdAttribute.getName() + "', '" + attribute.getName() + "'] in entity view '" + javaType.getName() + "'!");
                        } else {
                            foundIdAttribute = attribute;
                        }
                    }
                    
                    attributes.put(attribute.getName(), attribute);
                    addAttributeFilters(attribute);
                }
            }
        }
        
        if (foundIdAttribute == null) {
            throw new IllegalArgumentException("No id attribute was defined for entity view '" + javaType.getName() + "' although it is needed!");
        }

        this.idAttribute = foundIdAttribute;
        this.constructors = new HashMap<ParametersKey, MappingConstructor<X>>();
        this.constructorIndex = new HashMap<String, MappingConstructor<X>>();

        // TODO: This is probably not deterministic since the constructor order is not defined
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            String constructorName = MappingConstructorImpl.validate(this, constructor);
            if (constructorIndex.containsKey(constructorName)) {
                constructorName += constructorIndex.size();
            }
            MappingConstructor<X> mappingConstructor = new MappingConstructorImpl<X>(this, constructorName, (Constructor<X>) constructor, entityViews);
            constructors.put(new ParametersKey(constructor.getParameterTypes()), mappingConstructor);
            constructorIndex.put(constructorName, mappingConstructor);
        }
    }

    private void addFilterMapping(ViewFilter filterMapping) {
        String filterName = filterMapping.name();
        
        if (filterName.isEmpty()) {
            filterName = name;
            
            if (viewFilters.containsKey(filterName)) {
                throw new IllegalArgumentException("Illegal duplicate filter name mapping '" + filterName + "' at the class '" + javaType.getName() + "'!");
            }
        }
        
        ViewFilterMapping viewFilterMapping = new ViewFilterMappingImpl(this, filterName, filterMapping.value());
        viewFilters.put(viewFilterMapping.getName(), viewFilterMapping);
    }

    private void addAttributeFilters(AbstractMethodAttribute<? super X, ?> attribute) {
        for (Map.Entry<String, AttributeFilterMapping> entry : attribute.getFilterMappings().entrySet()) {
            String filterName = entry.getKey();
            AttributeFilterMapping filterMapping = entry.getValue();
            
            if (viewFilters.containsKey(filterName)) {
                throw new IllegalArgumentException("Illegal duplicate filter name mapping '" + filterName + "' at attribute '" + filterMapping.getDeclaringAttribute().getName() 
                    + "' of the class '" + javaType.getName() + "'! Already defined on class '" + javaType.getName() + "'!");
            } else if (attributeFilters.containsKey(filterName)) {
                attributeFilters.get(filterName);
                throw new IllegalArgumentException("Illegal duplicate filter name mapping '" + filterName + "' at attribute '" + filterMapping.getDeclaringAttribute().getName() 
                    + "' of the class '" + javaType.getName() + "'! Already defined on attribute class '" + javaType.getName() + "'!");
            }
            
            attributeFilters.put(filterName, filterMapping);
        }
    }

    // If you change something here don't forget to also update MappingConstructorImpl#createMethodAttribute
    private static <X> AbstractMethodAttribute<? super X, ?> createMethodAttribute(ViewType<X> viewType, Method method, Set<Class<?>> entityViews) {
        Annotation mapping = AbstractMethodAttribute.getMapping(viewType, method);
        if (mapping == null) {
            return null;
        }

        Class<?> attributeType = ReflectionUtils.getResolvedMethodReturnType(viewType.getJavaType(), method);
        
        // Force singular mapping
        if (AnnotationUtils.findAnnotation(method, MappingSingular.class) != null) {
            return new MethodMappingSingularAttributeImpl<X, Object>(viewType, method, mapping, entityViews);
        }

        if (Collection.class == attributeType) {
            return new MethodMappingCollectionAttributeImpl<X, Object>(viewType, method, mapping, entityViews);
        } else if (List.class == attributeType) {
            return new MethodMappingListAttributeImpl<X, Object>(viewType, method, mapping, entityViews);
        } else if (Set.class == attributeType || SortedSet.class == attributeType || NavigableSet.class == attributeType) {
            return new MethodMappingSetAttributeImpl<X, Object>(viewType, method, mapping, entityViews);
        } else if (Map.class == attributeType || SortedMap.class == attributeType || NavigableMap.class == attributeType) {
            return new MethodMappingMapAttributeImpl<X, Object, Object>(viewType, method, mapping, entityViews);
        } else if (mapping instanceof MappingSubquery) {
            return new MethodSubquerySingularAttributeImpl<X, Object>(viewType, method, mapping, entityViews);
        } else {
            return new MethodMappingSingularAttributeImpl<X, Object>(viewType, method, mapping, entityViews);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<X> getJavaType() {
        return javaType;
    }

    @Override
    public Class<?> getEntityClass() {
        return entityClass;
    }

    @Override
    public MethodAttribute<? super X, ?> getIdAttribute() {
        return idAttribute;
    }

    @Override
    public Set<MethodAttribute<? super X, ?>> getAttributes() {
        return new LinkedHashSet<MethodAttribute<? super X, ?>>(attributes.values());
    }

    @Override
    public MethodAttribute<? super X, ?> getAttribute(String name) {
        return attributes.get(name);
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
    public AttributeFilterMapping getAttributeFilter(String filterName) {
        return attributeFilters.get(filterName);
    }

    @Override
    public Set<AttributeFilterMapping> getAttributeFilters() {
        return new HashSet<AttributeFilterMapping>(attributeFilters.values());
    }

    @Override
    public ViewFilterMapping getViewFilter(String filterName) {
        return viewFilters.get(filterName);
    }

    @Override
    public Set<ViewFilterMapping> getViewFilters() {
        return new HashSet<ViewFilterMapping>(viewFilters.values());
    }

    @Override
    public Set<MappingConstructor<X>> getConstructors() {
        return new HashSet<MappingConstructor<X>>(constructors.values());
    }

    @Override
    public MappingConstructor<X> getConstructor(Class<?>... parameterTypes) {
        return constructors.get(new ParametersKey(parameterTypes));
    }

    @Override
    public Set<String> getConstructorNames() {
        return constructorIndex.keySet();
    }

    @Override
    public MappingConstructor<X> getConstructor(String name) {
        return constructorIndex.get(name);
    }

    private static class ParametersKey {

        private final Class<?>[] parameterTypes;

        public ParametersKey(Class<?>[] parameterTypes) {
            this.parameterTypes = parameterTypes;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + Arrays.deepHashCode(this.parameterTypes);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ParametersKey other = (ParametersKey) obj;
            if (!Arrays.deepEquals(this.parameterTypes, other.parameterTypes)) {
                return false;
            }
            return true;
        }
    }

}
