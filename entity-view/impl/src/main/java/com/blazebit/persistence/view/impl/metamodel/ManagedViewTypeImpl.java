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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import com.blazebit.persistence.impl.EntityMetamodel;
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

    @SuppressWarnings("unchecked")
    public ManagedViewTypeImpl(Class<? extends X> clazz, Class<?> entityClass, Set<Class<?>> entityViews, EntityMetamodel metamodel, ExpressionFactory expressionFactory, Set<String> errors) {
        this.javaType = (Class<X>) clazz;
        this.entityClass = entityClass;

        if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
            errors.add("Only interfaces or abstract classes are allowed as entity views. '" + clazz.getName() + "' is neither of those.");
        }

        BatchFetch batchFetch = AnnotationUtils.findAnnotation(clazz, BatchFetch.class);
        if (batchFetch == null || batchFetch.size() == -1) {
            this.defaultBatchSize = -1;
        } else if (batchFetch.size() < 1) {
            errors.add("Illegal batch fetch size defined at '" + clazz.getName() + "'! Use a value greater than 0 or -1!");
            this.defaultBatchSize = Integer.MIN_VALUE;
        } else {
            this.defaultBatchSize = batchFetch.size();
        }
        
        // We use a tree map to get a deterministic attribute order
        Map<String, AbstractMethodAttribute<? super X, ?>> attributes = new TreeMap<String, AbstractMethodAttribute<? super X, ?>>();

        // Deterministic order of methods for #203
        Method[] methods = clazz.getMethods();
        Set<String> handledMethods = new HashSet<String>(methods.length);
        Set<String> concreteMethods = new HashSet<String>(methods.length);
        // mark concrete methods as handled
        for (Method method : methods) {
            if (!Modifier.isAbstract(method.getModifiers()) && !method.isBridge()) {
                handledMethods.add(method.getName());
                concreteMethods.add(method.getName());
            }
        }
        for (Class<?> c : ReflectionUtils.getSuperTypes(clazz)) {
            for (Method method : c.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers()) && Modifier.isAbstract(method.getModifiers()) && !method.isBridge()) {
                    final String methodName = method.getName();
                    if (handledMethods.add(methodName)) {
                        handleMethod(method, entityViews, attributes, metamodel, expressionFactory, errors);
                    } else if (!concreteMethods.contains(methodName)) {
                        // Check if the attribute definition is conflicting
                        String attributeName = AbstractMethodAttribute.extractAttributeName(javaType, method);
                        Annotation mapping = AbstractMethodAttribute.getMapping(this, method);

                        // We ignore methods that only have implicit mappings
                        if (mapping instanceof MappingLiteral) {
                            continue;
                        }

                        AbstractMethodAttribute<? super X, ?> attribute = attributes.get(attributeName);
                        Annotation originalMapping = AbstractMethodAttribute.getMapping(this, attribute.getJavaMethod());

                        // If the mapping is the same, just let it through
                        if (mapping.equals(originalMapping)) {
                            continue;
                        }

                        // Also let through the attributes that are "specialized" in subclasses
                        if (method.getDeclaringClass() != attribute.getJavaMethod().getDeclaringClass()
                                && method.getDeclaringClass().isAssignableFrom(attribute.getJavaMethod().getDeclaringClass())) {
                            // The method is overridden/specialized by the method of the existing attribute
                            continue;
                        }

                        // If the original is implicitly mapped, but this attribute isn't, we have to replace it
                        if (originalMapping instanceof MappingLiteral) {
                            AbstractMethodAttribute<? super X, ?> newAttribute = createMethodAttribute(this, method, entityViews, metamodel, expressionFactory, errors);
                            attributes.put(newAttribute.getName(), newAttribute);
                            continue;
                        }

                        errors.add("Conflicting attribute mapping for attribute '" + attributeName + "' at the methods [" + methodReference(method) + ", " + methodReference(attribute.getJavaMethod()) + "] for managed view type '" + javaType.getName() + "'");
                    }
                }
            }
        }

        this.attributes = Collections.unmodifiableMap(attributes);
        Map<ParametersKey, MappingConstructorImpl<X>> constructors = new HashMap<ParametersKey, MappingConstructorImpl<X>>();
        Map<String, MappingConstructor<X>> constructorIndex = new HashMap<String, MappingConstructor<X>>();

        // TODO: This is probably not deterministic since the constructor order is not defined
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            String constructorName = MappingConstructorImpl.extractConstructorName(this, constructor);
            if (constructorIndex.containsKey(constructorName)) {
                constructorName += constructorIndex.size();
            }
            MappingConstructorImpl<X> mappingConstructor = new MappingConstructorImpl<X>(this, constructorName, (Constructor<X>) constructor, entityViews, metamodel, expressionFactory, errors);
            constructors.put(new ParametersKey(constructor.getParameterTypes()), mappingConstructor);
            constructorIndex.put(constructorName, mappingConstructor);
        }

        this.constructors = Collections.unmodifiableMap(constructors);
        this.constructorIndex = Collections.unmodifiableMap(constructorIndex);
    }

    private static String methodReference(Method method) {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }
    
    private void handleMethod(Method method, Set<Class<?>> entityViews, Map<String, AbstractMethodAttribute<? super X, ?>> attributes, EntityMetamodel metamodel, ExpressionFactory expressionFactory, Set<String> errors) {
        String attributeName = AbstractMethodAttribute.extractAttributeName(javaType, method);

        if (attributeName != null && !attributes.containsKey(attributeName)) {
            AbstractMethodAttribute<? super X, ?> attribute = createMethodAttribute(this, method, entityViews, metamodel, expressionFactory, errors);
            if (attribute != null) {
                attributes.put(attribute.getName(), attribute);
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

    private void addAttributeFilters(AbstractMethodAttribute<? super X, ?> attribute, Map<String, AttributeFilterMapping> attributeFilters, Set<String> errors) {
        for (Map.Entry<String, AttributeFilterMapping> entry : attribute.getFilterMappings().entrySet()) {
            String filterName = entry.getKey();
            AttributeFilterMapping filterMapping = entry.getValue();
            
            if (attributeFilters.containsKey(filterName)) {
                attributeFilters.get(filterName);
                errors.add("Illegal duplicate filter name mapping '" + filterName + "' at attribute '" + filterMapping.getDeclaringAttribute().getName()
                    + "' of the class '" + javaType.getName() + "'! Already defined on attribute class '" + javaType.getName() + "'!");
                continue;
            }
            
            attributeFilters.put(filterName, filterMapping);
        }
    }

    // If you change something here don't forget to also update MappingConstructorImpl#createMethodAttribute
    private static <X> AbstractMethodAttribute<? super X, ?> createMethodAttribute(ManagedViewType<X> viewType, Method method, Set<Class<?>> entityViews, EntityMetamodel metamodel, ExpressionFactory expressionFactory, Set<String> errors) {
        Annotation mapping = AbstractMethodAttribute.getMapping(viewType, method);
        if (mapping == null) {
            return null;
        }

        Class<?> attributeType = ReflectionUtils.getResolvedMethodReturnType(viewType.getJavaType(), method);
        
        // Force singular mapping
        if (AnnotationUtils.findAnnotation(method, MappingSingular.class) != null || mapping instanceof MappingParameter) {
            if (mapping instanceof MappingCorrelated) {
                return new CorrelatedMethodMappingSingularAttribute<X, Object>(viewType, method, mapping, entityViews, errors);
            } else {
                return new DefaultMethodMappingSingularAttribute<X, Object>(viewType, method, mapping, entityViews, errors);
            }
        }

        if (Collection.class == attributeType) {
            if (mapping instanceof MappingCorrelated) {
                return new CorrelatedMethodMappingCollectionAttribute<X, Object>(viewType, method, mapping, entityViews, errors);
            } else {
                return new DefaultMethodMappingCollectionAttribute<X, Object>(viewType, method, mapping, entityViews, errors);
            }
        } else if (List.class == attributeType) {
            if (mapping instanceof MappingCorrelated) {
                return new CorrelatedMethodMappingListAttribute<X, Object>(viewType, method, mapping, entityViews, metamodel, expressionFactory, errors);
            } else {
                return new DefaultMethodMappingListAttribute<X, Object>(viewType, method, mapping, entityViews, metamodel, expressionFactory, errors);
            }
        } else if (Set.class == attributeType || SortedSet.class == attributeType || NavigableSet.class == attributeType) {
            if (mapping instanceof MappingCorrelated) {
                return new CorrelatedMethodMappingSetAttribute<X, Object>(viewType, method, mapping, entityViews, errors);
            } else {
                return new DefaultMethodMappingSetAttribute<X, Object>(viewType, method, mapping, entityViews, errors);
            }
        } else if (Map.class == attributeType || SortedMap.class == attributeType || NavigableMap.class == attributeType) {
            if (mapping instanceof MappingCorrelated) {
                errors.add("The mapping defined on method '" + viewType.getJavaType().getName() + "." + method.getName() + "' uses a Map type with a correlated mapping which is unsupported!");
                return null;
            } else {
                return new DefaultMethodMappingMapAttribute<X, Object, Object>(viewType, method, mapping, entityViews, errors);
            }
        } else if (mapping instanceof MappingSubquery) {
            return new DefaultMethodSubquerySingularAttribute<X, Object>(viewType, method, mapping, entityViews, errors);
        } else if (mapping instanceof MappingCorrelated) {
            return new CorrelatedMethodMappingSingularAttribute<X, Object>(viewType, method, mapping, entityViews, errors);
        } else {
            return new DefaultMethodMappingSingularAttribute<X, Object>(viewType, method, mapping, entityViews, errors);
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
        return new SetView<MethodAttribute<? super X, ?>>(attributes.values());
    }

    @Override
    public MethodAttribute<? super X, ?> getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Set<MappingConstructor<X>> getConstructors() {
        return new SetView<MappingConstructor<X>>(constructors.values());
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
