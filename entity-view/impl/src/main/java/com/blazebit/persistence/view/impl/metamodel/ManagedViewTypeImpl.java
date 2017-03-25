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
import com.blazebit.persistence.view.BatchFetch;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;

import javax.persistence.metamodel.ManagedType;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
    protected final boolean hasJoinFetchedCollections;

    @SuppressWarnings("unchecked")
    public ManagedViewTypeImpl(ViewMapping viewMapping, MetamodelBuildingContext context) {
        this.javaType = (Class<X>) viewMapping.getEntityViewClass();
        this.entityClass = viewMapping.getMapping().value();

        if (!javaType.isInterface() && !Modifier.isAbstract(javaType.getModifiers())) {
            context.addError("Only interfaces or abstract classes are allowed as entity views. '" + javaType.getName() + "' is neither of those.");
        }

        BatchFetch batchFetch = AnnotationUtils.findAnnotation(javaType, BatchFetch.class);
        if (batchFetch == null || batchFetch.size() == -1) {
            this.defaultBatchSize = -1;
        } else if (batchFetch.size() < 1) {
            context.addError("Illegal batch fetch size defined at '" + javaType.getName() + "'! Use a value greater than 0 or -1!");
            this.defaultBatchSize = Integer.MIN_VALUE;
        } else {
            this.defaultBatchSize = batchFetch.size();
        }

        // We use a tree map to get a deterministic attribute order
        Map<String, AbstractMethodAttribute<? super X, ?>> attributes = new TreeMap<String, AbstractMethodAttribute<? super X, ?>>();
        boolean hasJoinFetchedCollections = false;

        for (MethodAttributeMapping mapping : viewMapping.getAttributes().values()) {
            AbstractMethodAttribute<? super X, ?> attribute = mapping.getMethodAttribute(this);
            hasJoinFetchedCollections = hasJoinFetchedCollections || attribute.hasJoinFetchedCollections();
            attributes.put(mapping.getAttributeName(), attribute);
        }

        this.attributes = Collections.unmodifiableMap(attributes);

        Map<ParametersKey, MappingConstructorImpl<X>> constructors = new HashMap<ParametersKey, MappingConstructorImpl<X>>();
        Map<String, MappingConstructor<X>> constructorIndex = new HashMap<String, MappingConstructor<X>>();

        for (Map.Entry<ParametersKey, ConstructorMapping> entry : viewMapping.getConstructors().entrySet()) {
            ConstructorMapping constructor = entry.getValue();
            String constructorName = constructor.getConstructorName();
            if (constructorIndex.containsKey(constructorName)) {
                constructorName += constructorIndex.size();
            }
            MappingConstructorImpl<X> mappingConstructor = new MappingConstructorImpl<X>(this, constructorName, constructor, context);
            constructors.put(entry.getKey(), mappingConstructor);
            constructorIndex.put(constructorName, mappingConstructor);
        }

        this.constructors = Collections.unmodifiableMap(constructors);
        this.constructorIndex = Collections.unmodifiableMap(constructorIndex);
        this.hasJoinFetchedCollections = hasJoinFetchedCollections;
    }

    public void checkAttributes(MetamodelBuildingContext context) {
        ManagedType<?> managedType = context.getEntityMetamodel().managedType(entityClass);
        Map<String, List<String>> collectionMappings = new HashMap<String, List<String>>();

        for (AbstractMethodAttribute<? super X, ?> attribute : attributes.values()) {
            attribute.checkAttribute(managedType, context);

            for (String mapping : attribute.getCollectionJoinMappings(managedType, context)) {
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
                
                constructor.checkParameters(managedType, constructorCollectionMappings, context);

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
                        context.addError(sb.toString());
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
                    context.addError(sb.toString());
                }
            }
        }
    }

    public void checkNestedAttributes(List<AbstractAttribute<?, ?>> parents, MetamodelBuildingContext context) {
        ManagedType<?> managedType = context.getEntityMetamodel().managedType(entityClass);

        for (AbstractMethodAttribute<? super X, ?> attribute : attributes.values()) {
            attribute.checkNestedAttribute(parents, managedType, context);
        }

        if (!constructors.isEmpty()) {
            for (MappingConstructorImpl<X> constructor : constructors.values()) {
                constructor.checkNestedParameters(parents, managedType, context);
            }
        }
    }

    protected abstract boolean hasId();

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

    public boolean hasJoinFetchedCollections() {
        return hasJoinFetchedCollections;
    }
}
