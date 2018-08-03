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

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.LockMode;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class ManagedViewTypeImpl<X> implements ManagedViewTypeImplementor<X> {

    private final Class<X> javaType;
    private final ManagedType<?> jpaManagedType;
    private final Method postCreateMethod;
    private final List<Method> specialMethods;
    private final boolean creatable;
    private final boolean updatable;
    private final boolean validatePersistability;
    private final LockMode lockMode;
    private final Set<String> excludedEntityAttributes;
    private final FlushMode flushMode;
    private final FlushStrategy flushStrategy;
    private final int defaultBatchSize;
    private final Map<String, AbstractMethodAttribute<? super X, ?>> attributes;
    private final Set<AbstractMethodAttribute<? super X, ?>> updateMappableAttributes;
    private final AbstractMethodAttribute<? super X, ?>[] mutableAttributes;
    private final Map<ParametersKey, MappingConstructorImpl<X>> constructors;
    private final Map<String, MappingConstructorImpl<X>> constructorIndex;
    private final String inheritanceMapping;
    private final InheritanceSubtypeConfiguration<X> defaultInheritanceSubtypeConfiguration;
    private final InheritanceSubtypeConfiguration<X> overallInheritanceSubtypeConfiguration;
    private final Map<Map<ManagedViewTypeImplementor<? extends X>, String>, InheritanceSubtypeConfiguration<X>> inheritanceSubtypeConfigurations;
    private final boolean hasJoinFetchedCollections;

    @SuppressWarnings("unchecked")
    public ManagedViewTypeImpl(ViewMapping viewMapping, ManagedType<?> managedType, MetamodelBuildingContext context) {
        this.javaType = (Class<X>) viewMapping.getEntityViewClass();
        this.jpaManagedType = managedType;
        this.postCreateMethod = viewMapping.getPostCreateMethod();
        this.specialMethods = viewMapping.getSpecialMethods();

        if (postCreateMethod != null) {
            Class<?>[] parameterTypes = postCreateMethod.getParameterTypes();
            if (!void.class.equals(postCreateMethod.getReturnType()) || parameterTypes.length > 1 || parameterTypes.length == 1 && !EntityViewManager.class.equals(parameterTypes[0])) {
                context.addError("Invalid signature for post create method at '" + javaType.getName() + "." + postCreateMethod.getName() + "'! A method annotated with @PostCreate must return void and accept no or a single EntityViewManager argument!");
            }
        }

        this.updatable = viewMapping.isUpdatable();
        this.flushMode = context.getFlushMode(javaType, viewMapping.getFlushMode());
        this.flushStrategy = context.getFlushStrategy(javaType, viewMapping.getFlushStrategy());
        this.lockMode = viewMapping.getResolvedLockMode();

        boolean embeddable = !(jpaManagedType instanceof EntityType<?>);

        if (viewMapping.isCreatable()) {
            if (embeddable && !updatable) {
                context.addError("Illegal creatable-only mapping at '" + javaType.getName() + "'! Declaring @CreatableEntityView for an entity view that maps a JPA embeddable type is only allowed when also @UpdatableEntityView is defined!");
            }
            this.creatable = true;
            this.validatePersistability = viewMapping.isValidatePersistability();
            if (validatePersistability) {
                this.excludedEntityAttributes = Collections.unmodifiableSet(new HashSet<>(viewMapping.getExcludedAttributes()));
            } else {
                this.excludedEntityAttributes = Collections.emptySet();
            }
        } else if (updatable && embeddable) {
            // If the entity view is for an embeddable, we also interpret it as creatable if it is marked as updatable
            this.creatable = true;
            this.validatePersistability = true;
            this.excludedEntityAttributes = Collections.emptySet();
        } else {
            this.creatable = false;
            this.validatePersistability = false;
            this.excludedEntityAttributes = Collections.emptySet();
        }

        if (!javaType.isInterface() && !Modifier.isAbstract(javaType.getModifiers())) {
            context.addError("Only interfaces or abstract classes are allowed as entity views. '" + javaType.getName() + "' is neither of those.");
        }

        Integer batchSize = viewMapping.getDefaultBatchSize();
        if (batchSize == null || batchSize == -1) {
            this.defaultBatchSize = -1;
        } else if (batchSize < 1) {
            context.addError("Illegal batch fetch size defined at '" + javaType.getName() + "'! Use a value greater than 0 or -1!");
            this.defaultBatchSize = Integer.MIN_VALUE;
        } else {
            this.defaultBatchSize = batchSize;
        }

        // We use a tree map to get a deterministic attribute order
        Map<String, AbstractMethodAttribute<? super X, ?>> attributes = new TreeMap<>();
        Set<AbstractMethodAttribute<? super X, ?>> updateMappableAttributes = new LinkedHashSet<>(attributes.size());
        List<AbstractMethodAttribute<? super X, ?>> mutableAttributes = new ArrayList<>(attributes.size());
        boolean hasJoinFetchedCollections = false;

        // Initialize attribute type and the dirty state index of attributes
        int index = viewMapping.getIdAttribute() == null ? 0 : 1;
        int dirtyStateIndex = 0;
        for (MethodAttributeMapping mapping : viewMapping.getMethodAttributes().values()) {
            AbstractMethodAttribute<? super X, ?> attribute;
            if (mapping.isId() || mapping.isVersion()) {
                // The id and the version always have -1 as dirty state index because they can't be dirty in the traditional sense
                // Id can only be set on "new" objects and shouldn't be mutable, version acts as optimistic concurrency version
                if (mapping.isId()) {
                    attribute = mapping.getMethodAttribute(this, 0, -1, context);
                    index--;
                } else {
                    attribute = mapping.getMethodAttribute(this, index, -1, context);
                }
            } else {
                // Note that the dirty state index is only a "suggested" index, but the implementation can choose not to use it
                attribute = mapping.getMethodAttribute(this, index, dirtyStateIndex, context);
                if (attribute.getDirtyStateIndex() != -1) {
                    mutableAttributes.add(attribute);
                    dirtyStateIndex++;
                }
            }
            hasJoinFetchedCollections = hasJoinFetchedCollections || attribute.hasJoinFetchedCollections();
            attributes.put(mapping.getName(), attribute);
            index++;
        }

        this.attributes = Collections.unmodifiableMap(attributes);

        for (AbstractMethodAttribute<? super X, ?> attribute : attributes.values()) {
            if (attribute.isUpdatable() || attribute.isUpdateMappable()) {
                updateMappableAttributes.add(attribute);
            }
        }

        this.mutableAttributes = mutableAttributes.toArray(new AbstractMethodAttribute[mutableAttributes.size()]);
        this.updateMappableAttributes = Collections.unmodifiableSet(updateMappableAttributes);
        Map<Map<ManagedViewTypeImplementor<? extends X>, String>, InheritanceSubtypeConfiguration<X>> inheritanceSubtypeConfigurations = new HashMap<>();
        Map<ViewMapping, String> overallInheritanceSubtypeMappings = new HashMap<>();

        for (InheritanceViewMapping inheritanceViewMapping : viewMapping.getInheritanceViewMappings()) {
            overallInheritanceSubtypeMappings.putAll(inheritanceViewMapping.getInheritanceSubtypeMappings());
        }

        this.overallInheritanceSubtypeConfiguration = new InheritanceSubtypeConfiguration<>(this, viewMapping, -1, new InheritanceViewMapping(overallInheritanceSubtypeMappings), context);
        this.defaultInheritanceSubtypeConfiguration = new InheritanceSubtypeConfiguration<>(this, viewMapping, 0, viewMapping.getDefaultInheritanceViewMapping(), context, overallInheritanceSubtypeConfiguration);

        inheritanceSubtypeConfigurations.put(defaultInheritanceSubtypeConfiguration.inheritanceSubtypeConfiguration, defaultInheritanceSubtypeConfiguration);
        int configurationIndex = 1;
        for (InheritanceViewMapping inheritanceViewMapping : viewMapping.getInheritanceViewMappings()) {
            // Skip the default as it is handled a few lines before
            if (inheritanceViewMapping != viewMapping.getDefaultInheritanceViewMapping()) {
                InheritanceSubtypeConfiguration<X> subtypeConfiguration = new InheritanceSubtypeConfiguration<>(this, viewMapping, configurationIndex, inheritanceViewMapping, context, overallInheritanceSubtypeConfiguration);
                inheritanceSubtypeConfigurations.put(subtypeConfiguration.inheritanceSubtypeConfiguration, subtypeConfiguration);
                configurationIndex++;
            }
        }

        this.inheritanceSubtypeConfigurations = Collections.unmodifiableMap(inheritanceSubtypeConfigurations);

        Map<ParametersKey, MappingConstructorImpl<X>> constructors = new HashMap<>();
        Map<String, MappingConstructorImpl<X>> constructorIndex = new TreeMap<>();

        for (Map.Entry<ParametersKey, ConstructorMapping> entry : viewMapping.getConstructorMappings().entrySet()) {
            ConstructorMapping constructor = entry.getValue();
            String constructorName = constructor.getName();
            // We do this just to get to the next step with the validation
            if (constructorIndex.containsKey(constructorName)) {
                constructorName += constructorIndex.size();
            }
            MappingConstructorImpl<X> mappingConstructor = new MappingConstructorImpl<X>(this, constructorName, constructor, context);
            constructors.put(entry.getKey(), mappingConstructor);
            constructorIndex.put(constructorName, mappingConstructor);
        }

        this.constructors = Collections.unmodifiableMap(constructors);
        this.constructorIndex = Collections.unmodifiableMap(constructorIndex);
        this.inheritanceMapping = viewMapping.determineInheritanceMapping(context);
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

    @Override
    public void checkAttributes(MetamodelBuildingContext context) {
        // Ensure that a plural entity attribute is not used multiple times in different plural entity view attributes
        // If it were used multiple times, the second collection would not receive all expected elements, because both are based on the same join
        // and the first collection will already cause a "fold" of the results for materializing the collection in the entity view
        // We could theoretically try to defer the "fold" action, but the current model makes this pretty hard. The obvious workaround is to map a plural subview attribute
        // and put all mappings into that. This will guarantee that the "fold" action only happens after all properties have been processed
        Map<String, List<String>> collectionMappings = new HashMap<String, List<String>>();

        for (AbstractMethodAttribute<? super X, ?> attribute : attributes.values()) {
            attribute.checkAttribute(jpaManagedType, context);

            for (String mapping : attribute.getCollectionJoinMappings(jpaManagedType, context)) {
                List<String> locations = collectionMappings.get(mapping);
                if (locations == null) {
                    locations = new ArrayList<>(2);
                    collectionMappings.put(mapping, locations);
                }

                locations.add("Attribute '" + attribute.getName() + "' in entity view '" + javaType.getName() + "'");
            }
        }
        
        if (!constructorIndex.isEmpty()) {
            for (MappingConstructorImpl<X> constructor : constructorIndex.values()) {
                Map<String, List<String>> constructorCollectionMappings = new HashMap<String, List<String>>();
                
                for (Map.Entry<String, List<String>> entry : collectionMappings.entrySet()) {
                    constructorCollectionMappings.put(entry.getKey(), new ArrayList<String>(entry.getValue()));
                }
                
                constructor.checkParameters(jpaManagedType, constructorCollectionMappings, context);

                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, List<String>> locationsEntry : constructorCollectionMappings.entrySet()) {
                    List<String> locations = locationsEntry.getValue();
                    if (locations.size() > 1) {
                        sb.setLength(0);
                        sb.append("Invalid multiple usages of the plural mapping '" + locationsEntry.getKey() + "'. Consider mapping the plural attribute only once with a subview. Problematic uses");
                        
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
                    sb.append("Invalid multiple usages of the plural mapping '" + locationsEntry.getKey() + "'. Consider mapping the plural attribute only once with a subview. Problematic uses");
                    
                    for (String location : locations) {
                        sb.append("\n - ");
                        sb.append(location);
                    }
                    context.addError(sb.toString());
                }
            }
        }
    }

    @Override
    public void checkNestedAttributes(List<AbstractAttribute<?, ?>> parents, MetamodelBuildingContext context) {
        for (AbstractMethodAttribute<? super X, ?> attribute : attributes.values()) {
            attribute.checkNestedAttribute(parents, jpaManagedType, context);
        }

        if (!constructorIndex.isEmpty()) {
            for (MappingConstructorImpl<X> constructor : constructorIndex.values()) {
                constructor.checkNestedParameters(parents, jpaManagedType, context);
            }
        }
    }

    protected abstract boolean hasId();

    @Override
    public boolean isUpdatable() {
        return updatable;
    }

    @Override
    public LockMode getLockMode() {
        return lockMode;
    }

    @Override
    public boolean isCreatable() {
        return creatable;
    }

    @Override
    public Method getPostCreateMethod() {
        return postCreateMethod;
    }

    @Override
    public List<Method> getSpecialMethods() {
        return specialMethods;
    }

    @Override
    public FlushMode getFlushMode() {
        return flushMode;
    }

    @Override
    public FlushStrategy getFlushStrategy() {
        return flushStrategy;
    }

    @Override
    public boolean isPersistabilityValidationEnabled() {
        return validatePersistability;
    }

    @Override
    public Set<String> getPersistabilityValidationExcludedEntityAttributes() {
        return excludedEntityAttributes;
    }

    @Override
    public Class<X> getJavaType() {
        return javaType;
    }

    @Override
    public Type getConvertedType() {
        return null;
    }

    @Override
    public TypeConverter<?, X> getConverter() {
        return null;
    }

    @Override
    public Class<?> getEntityClass() {
        return jpaManagedType.getJavaType();
    }

    @Override
    public ManagedType<?> getJpaManagedType() {
        return jpaManagedType;
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
    public Set<AbstractMethodAttribute<? super X, ?>> getUpdateMappableAttributes() {
        return updateMappableAttributes;
    }

    @Override
    public MethodAttribute<? super X, ?> getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Set<MappingConstructor<X>> getConstructors() {
        return new SetView<MappingConstructor<X>>(constructorIndex.values());
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
        if (name == null) {
            return null;
        }
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

    @Override
    public Map<ManagedViewTypeImplementor<? extends X>, String> getInheritanceSubtypeConfiguration() {
        return defaultInheritanceSubtypeConfiguration.inheritanceSubtypeConfiguration;
    }

    @Override
    public boolean hasJoinFetchedCollections() {
        return hasJoinFetchedCollections;
    }

    @Override
    public boolean hasSubtypes() {
        return defaultInheritanceSubtypeConfiguration.inheritanceSubtypes.size() > 1 || !defaultInheritanceSubtypeConfiguration.inheritanceSubtypes.contains(this);
    }

    @Override
    public int getSubtypeIndex(ManagedViewTypeImplementor<? super X> inheritanceBase) {
        int subtypeIndex = 0;
        for (ManagedViewType<?> subtype : inheritanceBase.getOverallInheritanceSubtypeConfiguration().getInheritanceSubtypes()) {
            if (subtype == this) {
                break;
            }
            subtypeIndex++;
        }
        return subtypeIndex;
    }

    @Override
    public InheritanceSubtypeConfiguration<X> getInheritanceSubtypeConfiguration(Map<ManagedViewTypeImplementor<? extends X>, String> inheritanceSubtypeMapping) {
        if (inheritanceSubtypeMapping == null || inheritanceSubtypeMapping.isEmpty() || defaultInheritanceSubtypeConfiguration.getInheritanceSubtypeConfiguration() == inheritanceSubtypeMapping) {
            return defaultInheritanceSubtypeConfiguration;
        }
        return inheritanceSubtypeConfigurations.get(inheritanceSubtypeMapping);
    }

    @Override
    public InheritanceSubtypeConfiguration<X> getOverallInheritanceSubtypeConfiguration() {
        return overallInheritanceSubtypeConfiguration;
    }

    @Override
    public InheritanceSubtypeConfiguration<X> getDefaultInheritanceSubtypeConfiguration() {
        return defaultInheritanceSubtypeConfiguration;
    }

    @Override
    public Map<Map<ManagedViewTypeImplementor<? extends X>, String>, InheritanceSubtypeConfiguration<X>> getInheritanceSubtypeConfigurations() {
        return inheritanceSubtypeConfigurations;
    }

    @Override
    public AbstractMethodAttribute<?, ?> getMutableAttribute(int i) {
        return mutableAttributes[i];
    }

    @Override
    public int getMutableAttributeCount() {
        return mutableAttributes.length;
    }

    public String getTypeConstraintMapping() {
        if (jpaManagedType instanceof EntityType<?>) {
            return "TYPE(this) = " + ((EntityType<?>) jpaManagedType).getName();
        }
        return null;
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
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

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class InheritanceSubtypeConfiguration<X> {
        private final ManagedViewTypeImpl<X> baseType;
        private final int configurationIndex;
        private final Map<ManagedViewTypeImplementor<? extends X>, String> inheritanceSubtypeConfiguration;
        private final Set<ManagedViewTypeImplementor<? extends X>> inheritanceSubtypes;
        private final String inheritanceDiscriminatorMapping;
        private final Map<AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>> attributesClosure;
        private final Map<ManagedViewTypeImplementor<? extends X>, int[]> overallPositionAssignments;

        public InheritanceSubtypeConfiguration(ManagedViewTypeImpl<X> baseType, ViewMapping baseTypeViewMapping, int configurationIndex, InheritanceViewMapping inheritanceViewMapping, MetamodelBuildingContext context) {
            this(baseType, baseTypeViewMapping, configurationIndex, inheritanceViewMapping, context, null);
        }

        public InheritanceSubtypeConfiguration(ManagedViewTypeImpl<X> baseType, ViewMapping baseTypeViewMapping, int configurationIndex, InheritanceViewMapping inheritanceViewMapping, MetamodelBuildingContext context, InheritanceSubtypeConfiguration<X> overallConfiguration) {
            this.baseType = baseType;
            this.configurationIndex = configurationIndex;
            ManagedViewTypeImpl<? extends X>[] orderedInheritanceSubtypes = createOrderedSubtypes(inheritanceViewMapping, context);
            this.inheritanceSubtypeConfiguration = createInheritanceSubtypeConfiguration(inheritanceViewMapping, context);
            this.inheritanceSubtypes = Collections.unmodifiableSet(inheritanceSubtypeConfiguration.keySet());
            this.inheritanceDiscriminatorMapping = createInheritanceDiscriminatorMapping(orderedInheritanceSubtypes);
            this.attributesClosure = createSubtypeAttributesClosure(orderedInheritanceSubtypes);
            Map<ManagedViewTypeImplementor<? extends X>, int[]> positionAssignments = new HashMap<>();

            // Map the attribute names to overall positions
            Map<AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>> overallAttributesClosure;
            if (overallConfiguration == null) {
                overallAttributesClosure = attributesClosure;
            } else {
                overallAttributesClosure = overallConfiguration.attributesClosure;
            }

            Map<String, Integer> overallPositionMap = new HashMap<>(overallAttributesClosure.size());
            int index = 0;
            String idName = null;
            if (baseType instanceof ViewTypeImpl<?>) {
                idName = baseTypeViewMapping.getIdAttribute().getName();
                overallPositionMap.put(idName, index);
                index++;
            }
            for (AttributeKey attributeKey : overallAttributesClosure.keySet()) {
                if (!attributeKey.attributeName.equals(idName)) {
                    overallPositionMap.put(attributeKey.attributeName, index);
                    index++;
                }
            }

            // Then create position assignments for all subtypes
            for (ManagedViewTypeImpl<? extends X> subtype : orderedInheritanceSubtypes) {
                // positionAssignment is a Map<TargetOverallConstructorPosition, StaticCreateFactoryParameterPosition>
                int[] positionAssignment = new int[overallPositionMap.size()];
                Arrays.fill(positionAssignment, -1);

                index = 0;
                if (idName != null) {
                    positionAssignment[index] = index;
                    index++;
                }
                // The attribute closure is in the order that we define the static create factory methods
                for (AttributeKey attributeKey : attributesClosure.keySet()) {
                    if (!attributeKey.attributeName.equals(idName)) {
                        // Only consider passing through attributes that exist in a subtype
                        if (subtype.getAttribute(attributeKey.getAttributeName()) != null) {
                            Integer position = overallPositionMap.get(attributeKey.attributeName);
                            if (position != null) {
                                positionAssignment[position] = index;
                            }
                        }
                        index++;
                    }
                }

                positionAssignments.put(subtype, positionAssignment);
            }
            this.overallPositionAssignments = Collections.unmodifiableMap(positionAssignments);
        }

        public ManagedViewTypeImplementor<X> getBaseType() {
            return baseType;
        }

        public int getConfigurationIndex() {
            return configurationIndex;
        }

        public Set<ManagedViewTypeImplementor<? extends X>> getInheritanceSubtypes() {
            return inheritanceSubtypes;
        }

        public Map<ManagedViewTypeImplementor<? extends X>, String> getInheritanceSubtypeConfiguration() {
            return inheritanceSubtypeConfiguration;
        }

        public String getInheritanceDiscriminatorMapping() {
            return inheritanceDiscriminatorMapping;
        }

        public Map<AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super X, ?>>> getAttributesClosure() {
            return attributesClosure;
        }

        public int[] getOverallPositionAssignment(ManagedViewTypeImplementor<? extends X> subtype) {
            return overallPositionAssignments.get(subtype);
        }

        @SuppressWarnings("unchecked")
        private ManagedViewTypeImpl<? extends X>[] createOrderedSubtypes(InheritanceViewMapping inheritanceViewMapping, MetamodelBuildingContext context) {
            ManagedViewTypeImpl<? extends X>[] orderedSubtypes = new ManagedViewTypeImpl[inheritanceViewMapping.getInheritanceSubtypeMappings().size()];
            int i = 0;
            for (ViewMapping mapping : inheritanceViewMapping.getInheritanceSubtypeMappings().keySet()) {
                if (mapping.getEntityViewClass() == baseType.javaType) {
                    orderedSubtypes[i++] = baseType;
                } else {
                    orderedSubtypes[i++] = (ManagedViewTypeImpl<X>) mapping.getManagedViewType(context);
                }
            }

            return orderedSubtypes;
        }

        @SuppressWarnings("unchecked")
        private Map<ManagedViewTypeImplementor<? extends X>, String> createInheritanceSubtypeConfiguration(InheritanceViewMapping inheritanceViewMapping, MetamodelBuildingContext context) {
            Map<ManagedViewTypeImplementor<? extends X>, String> configuration = new LinkedHashMap<>(inheritanceViewMapping.getInheritanceSubtypeMappings().size());

            for (Map.Entry<ViewMapping, String> mappingEntry : inheritanceViewMapping.getInheritanceSubtypeMappings().entrySet()) {
                String mapping = mappingEntry.getValue();
                if (mapping == null) {
                    mapping = mappingEntry.getKey().determineInheritanceMapping(context);
                    // An empty inheritance mapping signals that a subtype should actually be considered. If it was null it wouldn't be considered
                    if (mapping == null) {
                        mapping = "";
                    }
                }
                if (mappingEntry.getKey().getEntityViewClass() == baseType.javaType) {
                    configuration.put(baseType, mapping);
                } else {
                    configuration.put((ManagedViewTypeImplementor<? extends X>) mappingEntry.getKey().getManagedViewType(context), mapping);
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

            SortedMap<ManagedViewTypeImplementor<? extends X>, Integer> concreteToGeneralViewToDiscriminatorMap = new TreeMap<>(new Comparator<ManagedViewTypeImplementor<? extends X>>() {
                @Override
                public int compare(ManagedViewTypeImplementor<? extends X> o1, ManagedViewTypeImplementor<? extends X> o2) {
                    Class<? extends X> j1 = o1.getJavaType();
                    Class<? extends X> j2 = o2.getJavaType();
                    if (j1 == j2) {
                        return 0;
                    }
                    if (j1.isAssignableFrom(j2)) {
                        return 1;
                    }
                    if (j2.isAssignableFrom(j1)) {
                        return -1;
                    }

                    return j1.getName().compareTo(j2.getName());
                }
            });

            int subtypeIndex = 0;
            for (ManagedViewTypeImplementor<? extends X> subtype : subtypes) {
                concreteToGeneralViewToDiscriminatorMap.put(subtype, subtypeIndex++);
            }

            // Build the discriminator mapping expression
            StringBuilder sb = new StringBuilder();
            sb.append("CASE");
            for (Map.Entry<ManagedViewTypeImplementor<? extends X>, Integer> entry : concreteToGeneralViewToDiscriminatorMap.entrySet()) {
                ManagedViewTypeImplementor<?> subtype = entry.getKey();
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
                        if (subtypes[i].javaType.isAssignableFrom(subtype.getJavaType()) && inheritanceMapping != null && !inheritanceMapping.isEmpty()) {
                            // We only need to add the super type condition if the entity type is the same or the condition is not a type constraint
                            if (subtypes[i].getJpaManagedType() == subtype.getJpaManagedType() || !inheritanceMapping.equals(subtypes[i].getTypeConstraintMapping())) {
                                sb.append(" AND ").append(inheritanceMapping);
                            }
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
