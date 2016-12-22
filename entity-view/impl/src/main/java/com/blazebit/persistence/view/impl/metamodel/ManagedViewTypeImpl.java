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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
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

import javax.persistence.metamodel.ManagedType;

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.view.BatchFetch;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSingular;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedMethodMappingCollectionAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedMethodMappingListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedMethodMappingSetAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedMethodMappingSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.DefaultMethodMappingCollectionAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.DefaultMethodMappingListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.DefaultMethodMappingMapAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.DefaultMethodMappingSetAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.DefaultMethodMappingSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.DefaultMethodSubquerySingularAttribute;
import com.blazebit.persistence.view.metamodel.AttributeFilterMapping;
import com.blazebit.persistence.view.metamodel.FilterMapping;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.reflection.ReflectionUtils;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class ManagedViewTypeImpl<X> implements ManagedViewType<X> {

    protected final Class<X> javaType;
    protected final Class<?> entityClass;
    protected final int defaultBatchSize;
    protected final Map<String, AbstractMethodAttribute<? super X, ?>> attributes;
    protected final Map<ParametersKey, MappingConstructorImpl<X>> constructors;
    protected final Map<String, MappingConstructor<X>> constructorIndex;
    protected final Map<String, AttributeFilterMapping> attributeFilters;

    @SuppressWarnings("unchecked")
    public ManagedViewTypeImpl(Class<? extends X> clazz, Class<?> entityClass, Set<Class<?>> entityViews, EntityMetamodel metamodel, ExpressionFactory expressionFactory) {
        this.javaType = (Class<X>) clazz;
        this.entityClass = entityClass;

        if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
            throw new IllegalArgumentException("Only interfaces or abstract classes are allowed as entity views. '" + clazz.getName() + "' is neither of those.");
        }

        BatchFetch batchFetch = AnnotationUtils.findAnnotation(clazz, BatchFetch.class);
        if (batchFetch == null || batchFetch.size() == -1) {
            this.defaultBatchSize = -1;
        } else if (batchFetch.size() < 1) {
            throw new IllegalArgumentException("Illegal batch fetch size defined at '" + clazz.getName() + "'! Use a value greater than 0 or -1!");
        } else {
            this.defaultBatchSize = batchFetch.size();
        }
        
        // We use a tree map to get a deterministic attribute order
        this.attributes = new TreeMap<String, AbstractMethodAttribute<? super X, ?>>();
        this.attributeFilters = new HashMap<String, AttributeFilterMapping>();

        // Deterministic order of methods for #203
        Set<String> handledMethods = new HashSet<String>();
        // mark concrete methods as handled
        for (Method method : clazz.getMethods()) {
            if (!Modifier.isAbstract(method.getModifiers()) && !method.isBridge() && method.getParameterTypes().length == 0) {
                handledMethods.add(method.getName());
            }
        }
        for (Class<?> c : ReflectionUtils.getSuperTypes(clazz)) {
            for (Method method : c.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers()) && Modifier.isAbstract(method.getModifiers())) {
                    if (handledMethods.add(method.getName())) {
                        handleMethod(method, entityViews, metamodel, expressionFactory);
                    }
                }
            }
        }
        
        this.constructors = new HashMap<ParametersKey, MappingConstructorImpl<X>>();
        this.constructorIndex = new HashMap<String, MappingConstructor<X>>();

        // TODO: This is probably not deterministic since the constructor order is not defined
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            String constructorName = MappingConstructorImpl.validate(this, constructor);
            if (constructorIndex.containsKey(constructorName)) {
                constructorName += constructorIndex.size();
            }
            MappingConstructorImpl<X> mappingConstructor = new MappingConstructorImpl<X>(this, constructorName, (Constructor<X>) constructor, entityViews, metamodel, expressionFactory);
            constructors.put(new ParametersKey(constructor.getParameterTypes()), mappingConstructor);
            constructorIndex.put(constructorName, mappingConstructor);
        }
    }
    
    private void handleMethod(Method method, Set<Class<?>> entityViews, EntityMetamodel metamodel, ExpressionFactory expressionFactory) {
        String attributeName = AbstractMethodAttribute.validate(this, method);

        if (attributeName != null && !attributes.containsKey(attributeName)) {
            AbstractMethodAttribute<? super X, ?> attribute = createMethodAttribute(this, method, entityViews, metamodel, expressionFactory);
            if (attribute != null) {
                attributes.put(attribute.getName(), attribute);
                addAttributeFilters(attribute);
            }
        }
    }

    public void checkAttributesCorrelationUsage(Collection<String> errors, Map<Class<?>, String> seenCorrelationProviders, Map<Class<?>, ManagedViewTypeImpl<?>> managedViews, Set<ManagedViewType<?>> seenViewTypes, Set<MappingConstructor<?>> seenConstructors) {
        if (seenViewTypes.contains(this)) {
            return;
        }

        seenViewTypes.add(this);
        for (AbstractMethodAttribute<? super X, ?> attribute : attributes.values()) {
            attribute.checkAttributeCorrelationUsage(errors, seenCorrelationProviders, managedViews, seenViewTypes, seenConstructors);
        }

        if (!constructors.isEmpty()) {
            for (MappingConstructorImpl<X> constructor : constructors.values()) {
                constructor.checkParameterCorrelationUsage(errors, new HashMap<Class<?>, String>(seenCorrelationProviders), managedViews, new HashSet<ManagedViewType<?>>(seenViewTypes), new HashSet<MappingConstructor<?>>(seenConstructors));
            }
        }
    }
    
    public void checkAttributes(Map<Class<?>, ManagedViewTypeImpl<?>> managedViews, ExpressionFactory expressionFactory, EntityMetamodel metamodel, Set<String> errors) {
        ManagedType<?> managedType = metamodel.managedType(entityClass);
        Map<String, List<String>> collectionMappings = new HashMap<String, List<String>>();
        Map<Class<?>, String> seenCorrelationProviders = new HashMap<Class<?>, String>();
        Set<ManagedViewType<?>> seenViewTypes = new HashSet<ManagedViewType<?>>();
        Set<MappingConstructor<?>> seenConstructors = new HashSet<MappingConstructor<?>>();
        seenViewTypes.add(this);
        
        for (AbstractMethodAttribute<? super X, ?> attribute : attributes.values()) {
            errors.addAll(attribute.checkAttribute(managedType, managedViews, expressionFactory, metamodel));
            attribute.checkAttributeCorrelationUsage(errors, seenCorrelationProviders, managedViews, seenViewTypes, seenConstructors);

            for (String mapping : attribute.getCollectionJoinMappings(managedType, metamodel, expressionFactory)) {
                List<String> locations = collectionMappings.get(mapping);
                if (locations == null) {
                    locations = new ArrayList<String>(2);
                    collectionMappings.put(mapping, locations);
                }
                
                locations.add("Attribute '" + attribute.getName() + "' in entity view '" + javaType.getName() + "'");
            }
        }
        
        if (!constructors.isEmpty()) {
            for (MappingConstructorImpl<X> constructor : constructors.values()) {
                Map<String, List<String>> constructorCollectionMappings = new HashMap<String, List<String>>();
                
                for (Map.Entry<String, List<String>> entry : collectionMappings.entrySet()) {
                    constructorCollectionMappings.put(entry.getKey(), new ArrayList<String>(entry.getValue()));
                }
                
                constructor.checkParameters(managedType, managedViews, expressionFactory, metamodel, constructorCollectionMappings, errors);
                constructor.checkParameterCorrelationUsage(errors, new HashMap<Class<?>, String>(seenCorrelationProviders), managedViews, new HashSet<ManagedViewType<?>>(seenViewTypes), new HashSet<MappingConstructor<?>>(seenConstructors));

                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, List<String>> locationsEntry : constructorCollectionMappings.entrySet()) {
                    List<String> locations = locationsEntry.getValue();
                    if (locations.size() > 1) {
                        sb.setLength(0);
                        sb.append("Multiple usages of the mapping '" + locationsEntry.getKey() + "' in");
                        
                        for (String location : locations) {
                            sb.append("\n - ");
                            sb.append(location);
                        }
                        errors.add(sb.toString());
                    }
                }
            }
        } else {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<String>> locationsEntry : collectionMappings.entrySet()) {
                List<String> locations = locationsEntry.getValue();
                if (locations.size() > 1) {
                    sb.setLength(0);
                    sb.append("Multiple usages of the mapping '" + locationsEntry.getKey() + "' in");
                    
                    for (String location : locations) {
                        sb.append("\n - ");
                        sb.append(location);
                    }
                    errors.add(sb.toString());
                }
            }
        }
    }

    private void addAttributeFilters(AbstractMethodAttribute<? super X, ?> attribute) {
        for (Map.Entry<String, AttributeFilterMapping> entry : attribute.getFilterMappings().entrySet()) {
            String filterName = entry.getKey();
            AttributeFilterMapping filterMapping = entry.getValue();
            
            if (attributeFilters.containsKey(filterName)) {
                attributeFilters.get(filterName);
                throw new IllegalArgumentException("Illegal duplicate filter name mapping '" + filterName + "' at attribute '" + filterMapping.getDeclaringAttribute().getName()
                    + "' of the class '" + javaType.getName() + "'! Already defined on attribute class '" + javaType.getName() + "'!");
            }
            
            attributeFilters.put(filterName, filterMapping);
        }
    }

    // If you change something here don't forget to also update MappingConstructorImpl#createMethodAttribute
    private static <X> AbstractMethodAttribute<? super X, ?> createMethodAttribute(ManagedViewType<X> viewType, Method method, Set<Class<?>> entityViews, EntityMetamodel metamodel, ExpressionFactory expressionFactory) {
        Annotation mapping = AbstractMethodAttribute.getMapping(viewType, method);
        if (mapping == null) {
            return null;
        }

        Class<?> attributeType = ReflectionUtils.getResolvedMethodReturnType(viewType.getJavaType(), method);
        
        // Force singular mapping
        if (AnnotationUtils.findAnnotation(method, MappingSingular.class) != null || mapping instanceof MappingParameter) {
            if (mapping instanceof MappingCorrelated) {
                return new CorrelatedMethodMappingSingularAttribute<X, Object>(viewType, method, mapping, entityViews);
            } else {
                return new DefaultMethodMappingSingularAttribute<X, Object>(viewType, method, mapping, entityViews);
            }
        }

        if (Collection.class == attributeType) {
            if (mapping instanceof MappingCorrelated) {
                return new CorrelatedMethodMappingCollectionAttribute<X, Object>(viewType, method, mapping, entityViews);
            } else {
                return new DefaultMethodMappingCollectionAttribute<X, Object>(viewType, method, mapping, entityViews);
            }
        } else if (List.class == attributeType) {
            if (mapping instanceof MappingCorrelated) {
                return new CorrelatedMethodMappingListAttribute<X, Object>(viewType, method, mapping, entityViews, metamodel, expressionFactory);
            } else {
                return new DefaultMethodMappingListAttribute<X, Object>(viewType, method, mapping, entityViews, metamodel, expressionFactory);
            }
        } else if (Set.class == attributeType || SortedSet.class == attributeType || NavigableSet.class == attributeType) {
            if (mapping instanceof MappingCorrelated) {
                return new CorrelatedMethodMappingSetAttribute<X, Object>(viewType, method, mapping, entityViews);
            } else {
                return new DefaultMethodMappingSetAttribute<X, Object>(viewType, method, mapping, entityViews);
            }
        } else if (Map.class == attributeType || SortedMap.class == attributeType || NavigableMap.class == attributeType) {
            if (mapping instanceof MappingCorrelated) {
                throw new IllegalArgumentException("Map type unsupported for correlated mappings!");
            } else {
                return new DefaultMethodMappingMapAttribute<X, Object, Object>(viewType, method, mapping, entityViews);
            }
        } else if (mapping instanceof MappingSubquery) {
            return new DefaultMethodSubquerySingularAttribute<X, Object>(viewType, method, mapping, entityViews);
        } else if (mapping instanceof MappingCorrelated) {
            return new CorrelatedMethodMappingSingularAttribute<X, Object>(viewType, method, mapping, entityViews);
        } else {
            return new DefaultMethodMappingSingularAttribute<X, Object>(viewType, method, mapping, entityViews);
        }
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
    public int getDefaultBatchSize() {
        return defaultBatchSize;
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
        return attributeFilters.get(filterName);
    }

    @Override
    public Set<FilterMapping<?>> getFilters() {
        return new HashSet<FilterMapping<?>>(attributeFilters.size());
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
