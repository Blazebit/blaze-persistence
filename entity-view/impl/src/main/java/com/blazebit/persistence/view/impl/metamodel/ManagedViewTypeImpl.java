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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class ManagedViewTypeImpl<X> implements ManagedViewType<X> {

    private final Class<X> javaType;
    private final Class<?> entityClass;
    private final int defaultBatchSize;
    private final Map<String, AbstractMethodAttribute<? super X, ?>> attributes;
    private final Map<ParametersKey, MappingConstructorImpl<X>> constructors;
    private final Map<String, MappingConstructorImpl<X>> constructorIndex;
    private final String inheritanceMapping;
    private final InheritanceSubtypeConfiguration<X> defaultInheritanceSubtypeConfiguration;
    private final Map<Map<ManagedViewTypeImpl<? extends X>, String>, InheritanceSubtypeConfiguration<X>> inheritanceSubtypeConfigurations;
    private final boolean hasJoinFetchedCollections;

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
        Map<String, AbstractMethodAttribute<? super X, ?>> attributes = new TreeMap<>();
        boolean hasJoinFetchedCollections = false;

        for (MethodAttributeMapping mapping : viewMapping.getAttributes().values()) {
            AbstractMethodAttribute<? super X, ?> attribute = mapping.getMethodAttribute(this);
            hasJoinFetchedCollections = hasJoinFetchedCollections || attribute.hasJoinFetchedCollections();
            attributes.put(mapping.getAttributeName(), attribute);
        }

        this.attributes = Collections.unmodifiableMap(attributes);
        this.defaultInheritanceSubtypeConfiguration = new InheritanceSubtypeConfiguration<>(this, viewMapping.getDefaultInheritanceViewMapping());
        Map<Map<ManagedViewTypeImpl<? extends X>, String>, InheritanceSubtypeConfiguration<X>> inheritanceSubtypeConfigurations = new HashMap<>();
        inheritanceSubtypeConfigurations.put(defaultInheritanceSubtypeConfiguration.inheritanceSubtypeConfiguration, defaultInheritanceSubtypeConfiguration);
        for (InheritanceViewMapping inheritanceViewMapping : viewMapping.getInheritanceViewMappings()) {
            InheritanceSubtypeConfiguration<X> subtypeConfiguration = new InheritanceSubtypeConfiguration<>(this, inheritanceViewMapping);
            inheritanceSubtypeConfigurations.put(subtypeConfiguration.inheritanceSubtypeConfiguration, subtypeConfiguration);
        }

        this.inheritanceSubtypeConfigurations = Collections.unmodifiableMap(inheritanceSubtypeConfigurations);

        Map<ParametersKey, MappingConstructorImpl<X>> constructors = new HashMap<>();
        Map<String, MappingConstructorImpl<X>> constructorIndex = new HashMap<>();

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
        this.inheritanceMapping = viewMapping.getInheritanceMapping();
        this.hasJoinFetchedCollections = hasJoinFetchedCollections;

        if (viewMapping.getInheritanceSupertypes().isEmpty()) {
            if (inheritanceMapping != null) {
                context.addError("Entity view type '" + javaType.getName() + "' has a @EntityViewInheritanceMapping but is never used as subtype which is not allowed!");
            }
        } else {
            if (inheritanceMapping == null || inheritanceMapping.isEmpty()) {
                List<Class<?>> classes = new ArrayList<>();
                for (ViewMapping mapping : viewMapping.getInheritanceSupertypes()) {
                    classes.add(mapping.getEntityViewClass());
                }
                context.addError("Entity view type '" + javaType.getName() + "' has no @EntityViewInheritanceMapping but is used as inheritance subtype in: " + classes);
            }
        }
    }

    public void checkAttributes(MetamodelBuildingContext context) {
        ManagedType<?> managedType = context.getEntityMetamodel().managedType(entityClass);
        // Ensure that a plural entity attribute is not used multiple times in different plural entity view attributes
        // If it were used multiple times, the second collection would not receive all expected elements, because both are based on the same join
        // and the first collection will already cause a "fold" of the results for materializing the collection in the entity view
        // We could theoretically try to defer the "fold" action, but the current model makes this pretty hard. The obvious workaround is to map a plural subview attribute
        // and put all mappings into that. This will guarantee that the "fold" action only happens after all properties have been processed
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

    public boolean isUpdatable() {
        return false;
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
    public MappingConstructorImpl<X> getConstructor(String name) {
        return constructorIndex.get(name);
    }

    @Override
    public String getInheritanceMapping() {
        return inheritanceMapping;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<ManagedViewType<? extends X>> getInheritanceSubtypes() {
        return (Set<ManagedViewType<? extends X>>) (Set<?>) defaultInheritanceSubtypeConfiguration.inheritanceSubtypes;
    }

    public Map<ManagedViewTypeImpl<? extends X>, String> getInheritanceSubtypeConfiguration() {
        return defaultInheritanceSubtypeConfiguration.inheritanceSubtypeConfiguration;
    }

    public boolean hasJoinFetchedCollections() {
        return hasJoinFetchedCollections;
    }

    public boolean hasSubtypes() {
        return defaultInheritanceSubtypeConfiguration.inheritanceSubtypes.size() > 1 || !defaultInheritanceSubtypeConfiguration.inheritanceSubtypes.contains(this);
    }

    public InheritanceSubtypeConfiguration<X> getInheritanceSubtypeConfiguration(Map<ManagedViewTypeImpl<? extends X>, String> inheritanceSubtypeMapping) {
        if (inheritanceSubtypeMapping == null || inheritanceSubtypeMapping.isEmpty() || defaultInheritanceSubtypeConfiguration.getInheritanceSubtypeConfiguration() == inheritanceSubtypeMapping) {
            return defaultInheritanceSubtypeConfiguration;
        }
        return inheritanceSubtypeConfigurations.get(inheritanceSubtypeMapping);
    }

    public InheritanceSubtypeConfiguration<X> getDefaultInheritanceSubtypeConfiguration() {
        return defaultInheritanceSubtypeConfiguration;
    }

    public Map<Map<ManagedViewTypeImpl<? extends X>, String>, InheritanceSubtypeConfiguration<X>> getInheritanceSubtypeConfigurations() {
        return inheritanceSubtypeConfigurations;
    }

    public static final class AttributeKey {
        final int subtypeIndex;
        final String attributeName;

        public AttributeKey(int subtypeIndex, String attributeName) {
            this.subtypeIndex = subtypeIndex;
            this.attributeName = attributeName;
        }

        public int getSubtypeIndex() {
            return subtypeIndex;
        }

        public String getAttributeName() {
            return attributeName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AttributeKey)) {
                return false;
            }

            AttributeKey that = (AttributeKey) o;

            if (subtypeIndex != -1 && that.subtypeIndex != -1 && subtypeIndex != that.subtypeIndex) {
                return false;
            }
            return attributeName.equals(that.attributeName);
        }

        @Override
        public int hashCode() {
            return attributeName.hashCode();
        }
    }

    public static class InheritanceSubtypeConfiguration<X> {
        private final ManagedViewTypeImpl<X> baseType;
        private final Map<ManagedViewTypeImpl<? extends X>, String> inheritanceSubtypeConfiguration;
        private final Set<ManagedViewTypeImpl<? extends X>> inheritanceSubtypes;
        private final String inheritanceDiscriminatorMapping;
        private final Map<AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>> attributesClosure;

        public InheritanceSubtypeConfiguration(ManagedViewTypeImpl<X> baseType, InheritanceViewMapping inheritanceViewMapping) {
            this.baseType = baseType;
            ManagedViewTypeImpl<? extends X>[] orderedInheritanceSubtypes = createOrderedSubtypes(inheritanceViewMapping);
            this.inheritanceSubtypeConfiguration = createInheritanceSubtypeConfiguration(inheritanceViewMapping);
            this.inheritanceSubtypes = Collections.unmodifiableSet(inheritanceSubtypeConfiguration.keySet());
            this.inheritanceDiscriminatorMapping = createInheritanceDiscriminatorMapping(orderedInheritanceSubtypes);
            this.attributesClosure = createSubtypeAttributesClosure(orderedInheritanceSubtypes);
        }

        public ManagedViewTypeImpl<X> getBaseType() {
            return baseType;
        }

        public Set<ManagedViewTypeImpl<? extends X>> getInheritanceSubtypes() {
            return inheritanceSubtypes;
        }

        public Map<ManagedViewTypeImpl<? extends X>, String> getInheritanceSubtypeConfiguration() {
            return inheritanceSubtypeConfiguration;
        }

        public String getInheritanceDiscriminatorMapping() {
            return inheritanceDiscriminatorMapping;
        }

        public Map<AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>> getAttributesClosure() {
            return attributesClosure;
        }

        @SuppressWarnings("unchecked")
        private ManagedViewTypeImpl<? extends X>[] createOrderedSubtypes(InheritanceViewMapping inheritanceViewMapping) {
            ManagedViewTypeImpl<? extends X>[] orderedSubtypes = new ManagedViewTypeImpl[inheritanceViewMapping.getInheritanceSubtypeMappings().size()];
            int i = 0;
            for (ViewMapping mapping : inheritanceViewMapping.getInheritanceSubtypeMappings().keySet()) {
                if (mapping.getEntityViewClass() == baseType.javaType) {
                    orderedSubtypes[i++] = baseType;
                } else {
                    orderedSubtypes[i++] = (ManagedViewTypeImpl<X>) mapping.getManagedViewType();
                }
            }

            return orderedSubtypes;
        }

        @SuppressWarnings("unchecked")
        private Map<ManagedViewTypeImpl<? extends X>, String> createInheritanceSubtypeConfiguration(InheritanceViewMapping inheritanceViewMapping) {
            Map<ManagedViewTypeImpl<? extends X>, String> configuration = new LinkedHashMap<>(inheritanceViewMapping.getInheritanceSubtypeMappings().size());

            for (Map.Entry<ViewMapping, String> mappingEntry : inheritanceViewMapping.getInheritanceSubtypeMappings().entrySet()) {
                String mapping = mappingEntry.getValue();
                if (mapping == null) {
                    mapping = mappingEntry.getKey().getInheritanceMapping();
                    // An empty inheritance mapping signals that a subtype should actually be considered. If it was null it wouldn't be considered
                    if (mapping == null) {
                        mapping = "";
                    }
                }
                if (mappingEntry.getKey().getEntityViewClass() == baseType.javaType) {
                    configuration.put(baseType, mapping);
                } else {
                    configuration.put((ManagedViewTypeImpl<X>) mappingEntry.getKey().getManagedViewType(), mapping);
                }
            }

            return configuration;
        }

        public boolean hasSubtypes() {
            return inheritanceDiscriminatorMapping != null;
        }

        @SuppressWarnings("unchecked")
        private String createInheritanceDiscriminatorMapping(ManagedViewTypeImpl<? extends X>[] subtypes) {
            if (subtypes.length == 1 && subtypes[0] == baseType) {
                return null;
            }

            SortedMap<ManagedViewTypeImpl<? extends X>, Integer> concreteToGeneralViewToDiscriminatorMap = new TreeMap<>(new Comparator<ManagedViewTypeImpl<? extends X>>() {
                @Override
                public int compare(ManagedViewTypeImpl<? extends X> o1, ManagedViewTypeImpl<? extends X> o2) {
                    if (o1.javaType == o2.javaType) {
                        return 0;
                    }
                    if (o1.javaType.isAssignableFrom(o2.javaType)) {
                        return 1;
                    }
                    if (o2.javaType.isAssignableFrom(o1.javaType)) {
                        return -1;
                    }

                    return o1.javaType.getName().compareTo(o2.javaType.getName());
                }
            });

            int subtypeIndex = 0;
            for (ManagedViewTypeImpl<? extends X> subtype : subtypes) {
                concreteToGeneralViewToDiscriminatorMap.put(subtype, subtypeIndex++);
            }

            // Build the discriminator mapping expression
            StringBuilder sb = new StringBuilder();
            sb.append("CASE");
            for (Map.Entry<ManagedViewTypeImpl<? extends X>, Integer> entry : concreteToGeneralViewToDiscriminatorMap.entrySet()) {
                ManagedViewTypeImpl<?> subtype = entry.getKey();
                subtypeIndex = entry.getValue();
                if (subtype == baseType) {
                    continue;
                }
                String inheritanceMapping = inheritanceSubtypeConfiguration.get(subtype);

                if (inheritanceMapping != null && !inheritanceMapping.isEmpty()) {
                    sb.append(" WHEN ");
                    sb.append(inheritanceMapping);

                    for (int i = subtypeIndex - 1; i >= 0; i--) {
                        inheritanceMapping = inheritanceSubtypeConfiguration.get(subtypes[i]);
                        if (subtypes[i].javaType.isAssignableFrom(subtype.javaType) && inheritanceMapping != null && !inheritanceMapping.isEmpty()) {
                            sb.append(" AND ").append(inheritanceMapping);
                        }
                    }

                    sb.append(" THEN ");
                    sb.append(subtypeIndex);
                }
            }

            // We only consider mappings that are non-null
            String defaultMapping = inheritanceSubtypeConfiguration.get(baseType);
            if (defaultMapping != null) {
                if (defaultMapping.isEmpty()) {
                    sb.append(" ELSE 0");
                } else {
                    sb.append(" WHEN ").append(defaultMapping).append(" THEN 0");
                }
            }

            sb.append(" END");
            return sb.toString();
        }

        @SuppressWarnings("unchecked")
        private Map<AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>> createSubtypeAttributesClosure(ManagedViewTypeImpl<? extends X>[] subtypes) {
            int subtypeIndex = 0;
            // Collect all attributes from all subtypes in separate maps
            Map<AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>> subtypesAttributesClosure = new LinkedHashMap<>();

            for (AbstractMethodAttribute<? super X, ?> attribute : baseType.attributes.values()) {
                subtypesAttributesClosure.put(new AttributeKey(0, attribute.getName()), new ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>(null, attribute));
            }

            if (subtypes.length > 0) {
                // Go through the subtype attributes and put them in the attribute closure maps
                for (ManagedViewTypeImpl<? extends X> subtype : subtypes) {
                    subtypeIndex++;

                    for (AbstractMethodAttribute<? super X, ?> attribute : (Collection<AbstractMethodAttribute<? super X, ?>>) (Collection<?>) subtype.attributes.values()) {
                        // Try to find the attribute on some of the super types to see if it is specialized
                        ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>> superTypeAttribute = findAttribute(subtypesAttributesClosure, subtypes, subtypeIndex, attribute.getName());
                        if (superTypeAttribute != null) {
                            if (!attribute.getJavaMethod().equals(superTypeAttribute.getAttribute().getJavaMethod())) {
                                // This attribute was overridden/specialized in a subtype
                                superTypeAttribute.addSelectionConstraint(inheritanceSubtypeConfiguration.get(subtype), attribute);
                            }
                            // Otherwise the attribute came from the parent so we ignore it
                        } else {
                            subtypesAttributesClosure.put(new AttributeKey(subtypeIndex, attribute.getName()), new ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>(inheritanceSubtypeConfiguration.get(subtype), attribute));
                        }
                    }
                }
            }

            return subtypesAttributesClosure;
        }

        @SuppressWarnings("unchecked")
        private ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>> findAttribute(Map<AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>> subtypesAttributesClosure, ManagedViewTypeImpl<?>[] subtypes, int subtypeIndex, String name) {
            for (int i = subtypeIndex - 1; i >= 0; i--) {
                // Must be a proper subtype and contain an attribute with that name
                if (subtypes[i].javaType.isAssignableFrom(subtypes[i].javaType) && subtypes[i].getAttribute(name) != null) {
                    // Only then we will try to find the constrained attribute
                    ConstrainedAttribute<?> attribute = subtypesAttributesClosure.get(new AttributeKey(i, name));
                    if (attribute != null) {
                        return (ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>) attribute;
                    }
                }
            }

            return null;
        }
    }
}
