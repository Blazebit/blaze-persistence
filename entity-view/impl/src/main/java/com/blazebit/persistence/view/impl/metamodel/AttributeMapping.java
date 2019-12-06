/*
 * Copyright 2014 - 2019 Blazebit.
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

import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.impl.ScalarTargetResolvingExpressionVisitor;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.spi.EntityViewAttributeMapping;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AttributeMapping implements EntityViewAttributeMapping {

    private static final Logger LOG = Logger.getLogger("com.blazebit.persistence.view.SUBTYPE_INFERENCE");

    protected final ViewMapping viewMapping;
    protected final Annotation mapping;
    protected final MetamodelBootContext context;

    // Java types
    protected final boolean isCollection;
    protected final Class<?> declaredTypeClass;
    protected final Class<?> declaredKeyTypeClass;
    protected final Class<?> declaredElementTypeClass;
    protected final java.lang.reflect.Type declaredType;
    protected final java.lang.reflect.Type declaredKeyType;
    protected final java.lang.reflect.Type declaredElementType;
    protected final Map<Class<?>, String> inheritanceSubtypeClassMappings;
    protected final Map<Class<?>, String> keyInheritanceSubtypeClassMappings;
    protected final Map<Class<?>, String> elementInheritanceSubtypeClassMappings;

    // Basic configs
    protected ContainerBehavior containerBehavior;
    protected Class<? extends Comparator<?>> comparatorClass;
    protected boolean forceUniqueness;
    protected Boolean disallowOwnedUpdatableSubview;

    // Other configs
    protected Integer defaultBatchSize;

    // Resolved types
    protected boolean resolvedTypeMappings;
    protected List<ScalarTargetResolvingExpressionVisitor.TargetType> possibleTargets;
    protected Type<?> type;
    protected Type<?> keyType;
    protected Type<?> elementType;
    protected ViewMapping typeMapping;
    protected ViewMapping keyViewMapping;
    protected ViewMapping elementViewMapping;
    protected Map<EmbeddableOwner, Type<?>> embeddableTypeMap;
    protected Map<EmbeddableOwner, Type<?>> embeddableKeyTypeMap;
    protected Map<EmbeddableOwner, Type<?>> embeddableElementTypeMap;

    protected InheritanceViewMapping inheritanceSubtypeMappings;
    protected InheritanceViewMapping keyInheritanceSubtypeMappings;
    protected InheritanceViewMapping elementInheritanceSubtypeMappings;
    protected Map<ManagedViewTypeImplementor<?>, String> inheritanceSubtypes;
    protected Map<ManagedViewTypeImplementor<?>, String> keyInheritanceSubtypes;
    protected Map<ManagedViewTypeImplementor<?>, String> elementInheritanceSubtypes;
    protected Map<EmbeddableOwner, Map<ManagedViewTypeImplementor<?>, String>> embeddableInheritanceSubtypesMap;
    protected Map<EmbeddableOwner, Map<ManagedViewTypeImplementor<?>, String>> embeddableKeyInheritanceSubtypesMap;
    protected Map<EmbeddableOwner, Map<ManagedViewTypeImplementor<?>, String>> embeddableElementInheritanceSubtypesMap;

    protected AbstractAttribute<?, ?> attribute;
    protected Map<EmbeddableOwner, AbstractAttribute<?, ?>> embeddableAttributeMap;

    public AttributeMapping(ViewMapping viewMapping, Annotation mapping, MetamodelBootContext context, boolean isCollection, Class<?> declaredTypeClass, Class<?> declaredKeyTypeClass, Class<?> declaredElementTypeClass,
                            java.lang.reflect.Type declaredType, java.lang.reflect.Type declaredKeyType, java.lang.reflect.Type declaredElementType, Map<Class<?>, String> inheritanceSubtypeClassMappings, Map<Class<?>, String> keyInheritanceSubtypeClassMappings, Map<Class<?>, String> elementInheritanceSubtypeClassMappings) {
        this.viewMapping = viewMapping;
        this.mapping = mapping;
        this.context = context;
        this.isCollection = isCollection;
        this.declaredTypeClass = declaredTypeClass;
        this.declaredKeyTypeClass = declaredKeyTypeClass;
        this.declaredElementTypeClass = declaredElementTypeClass;
        this.declaredType = declaredType;
        this.declaredKeyType = declaredKeyType;
        this.declaredElementType = declaredElementType;
        this.inheritanceSubtypeClassMappings = inheritanceSubtypeClassMappings;
        this.keyInheritanceSubtypeClassMappings = keyInheritanceSubtypeClassMappings;
        this.elementInheritanceSubtypeClassMappings = elementInheritanceSubtypeClassMappings;
    }

    public Annotation getMapping() {
        return mapping;
    }

    public ViewMapping getKeyViewMapping() {
        return keyViewMapping;
    }

    public ViewMapping getElementViewMapping() {
        return elementViewMapping;
    }

    public abstract boolean isId();

    public abstract boolean isVersion();

    @Override
    public boolean isCollection() {
        return isCollection;
    }

    @Override
    public ContainerBehavior getContainerBehavior() {
        return containerBehavior;
    }

    @Override
    public void setContainerDefault() {
        this.containerBehavior = ContainerBehavior.DEFAULT;
        this.comparatorClass = null;
    }

    @Override
    public void setContainerIndexed() {
        this.containerBehavior = ContainerBehavior.INDEXED;
        this.comparatorClass = null;
    }

    @Override
    public void setContainerOrdered() {
        this.containerBehavior = ContainerBehavior.ORDERED;
        this.comparatorClass = null;
    }

    @Override
    public void setContainerSorted(Class<? extends Comparator<?>> comparatorClass) {
        this.containerBehavior = ContainerBehavior.SORTED;
        this.comparatorClass = comparatorClass;
    }

    @Override
    public boolean isForceUniqueness() {
        return forceUniqueness;
    }

    @Override
    public void setForceUniqueness(boolean forceUniqueness) {
        this.forceUniqueness = forceUniqueness;
    }

    @Override
    public boolean isDisallowOwnedUpdatableSubview() {
        return !Boolean.FALSE.equals(disallowOwnedUpdatableSubview);
    }

    @Override
    public void setDisallowOwnedUpdatableSubview(boolean disallowOwnedUpdatableSubview) {
        this.disallowOwnedUpdatableSubview = disallowOwnedUpdatableSubview;
    }

    @Override
    public Class<? extends Comparator<?>> getComparatorClass() {
        return comparatorClass;
    }

    @Override
    public Integer getDefaultBatchSize() {
        return defaultBatchSize;
    }

    @Override
    public void setDefaultBatchSize(Integer defaultBatchSize) {
        this.defaultBatchSize = defaultBatchSize;
    }

    public abstract String getErrorLocation();

    public abstract String getMappedBy();

    public abstract InverseRemoveStrategy getInverseRemoveStrategy();

    public boolean isSorted() {
        return containerBehavior == ContainerBehavior.SORTED;
    }

    public abstract boolean determineDisallowOwnedUpdatableSubview(MetamodelBuildingContext context, EmbeddableOwner embeddableMapping, Attribute<?, ?> updateMappableAttribute);

    public abstract String determineMappedBy(ManagedType<?> managedType, String mapping, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping);

    public abstract Map<String, String> determineWritableMappedByMappings(ManagedType<?> managedType, String mappedBy, MetamodelBuildingContext context);

    public boolean determineIndexed(MetamodelBuildingContext context, ManagedType<?> managedType) {
        if (containerBehavior != null) {
            return containerBehavior == ContainerBehavior.INDEXED;
        }

        String mappingExpression;
        if (mapping instanceof IdMapping) {
            mappingExpression = ((IdMapping) mapping).value();
        } else if (mapping instanceof Mapping) {
            mappingExpression = ((Mapping) mapping).value();
        } else {
            // Correlated mappings, parameter mappings and subqueries are never indexed
            containerBehavior = ContainerBehavior.DEFAULT;
            return false;
        }
        if (MetamodelUtils.isIndexedList(context.getEntityMetamodel(), context.getExpressionFactory(), managedType.getJavaType(), AbstractAttribute.stripThisFromMapping(mappingExpression))) {
            containerBehavior = ContainerBehavior.INDEXED;
            return true;
        } else {
            containerBehavior = ContainerBehavior.DEFAULT;
            return false;
        }
    }

    @Override
    public Class<?> getDeclaredType() {
        return declaredTypeClass;
    }

    @Override
    public Class<?> getDeclaredKeyType() {
        return declaredKeyTypeClass;
    }

    @Override
    public Class<?> getDeclaredElementType() {
        return declaredElementTypeClass;
    }

    public Class<?> getJavaType(MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        Type<?> t = getType(context, embeddableMapping);
        if (t == null) {
            return null;
        }
        return t.getJavaType();
    }

    public List<ScalarTargetResolvingExpressionVisitor.TargetType> getPossibleTargetTypes(MetamodelBuildingContext context) {
        if (possibleTargets != null) {
            return possibleTargets;
        }
        return possibleTargets = context.getPossibleTargetTypes(viewMapping.getEntityClass(), getMapping());
    }

    public Set<Class<?>> getBaseTypes(List<ScalarTargetResolvingExpressionVisitor.TargetType> possibleTargetTypes) {
        if (possibleTargetTypes.isEmpty()) {
            return Collections.singleton(null);
        }
        Set<Class<?>> baseTypes = new HashSet<>(possibleTargetTypes.size());
        for (ScalarTargetResolvingExpressionVisitor.TargetType possibleTargetType : possibleTargetTypes) {
            baseTypes.add(possibleTargetType.getLeafBaseClass());
        }
        return baseTypes;
    }

    public Set<Class<?>> getKeyTypes(List<ScalarTargetResolvingExpressionVisitor.TargetType> possibleTargetTypes) {
        if (possibleTargetTypes.isEmpty()) {
            return Collections.singleton(null);
        }
        Set<Class<?>> baseTypes = new HashSet<>(possibleTargetTypes.size());
        for (ScalarTargetResolvingExpressionVisitor.TargetType possibleTargetType : possibleTargetTypes) {
            baseTypes.add(possibleTargetType.getLeafBaseKeyClass());
        }
        return baseTypes;
    }

    public Set<Class<?>> getElementTypes(List<ScalarTargetResolvingExpressionVisitor.TargetType> possibleTargetTypes) {
        if (possibleTargetTypes.isEmpty()) {
            return Collections.singleton(null);
        }
        Set<Class<?>> baseTypes = new HashSet<>(possibleTargetTypes.size());
        for (ScalarTargetResolvingExpressionVisitor.TargetType possibleTargetType : possibleTargetTypes) {
            baseTypes.add(possibleTargetType.getLeafBaseValueClass());
        }
        return baseTypes;
    }

    public Type<?> getType(MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        if (embeddableMapping == null) {
            if (type != null) {
                return type;
            }
            if (typeMapping == null) {
                return type = context.getBasicType(viewMapping, declaredType, declaredTypeClass, getBaseTypes(getPossibleTargetTypes(context)));
            }
            return type = context.getManagedViewType(typeMapping, embeddableMapping);
        } else {
            if (embeddableTypeMap == null) {
                embeddableTypeMap = new HashMap<>(1);
            }
            Type<?> t = embeddableTypeMap.get(embeddableMapping);
            if (t != null) {
                return t;
            }
            if (typeMapping == null) {
                t = context.getBasicType(viewMapping, declaredType, declaredTypeClass, getBaseTypes(getPossibleTargetTypes(context)));
            } else {
                t = context.getManagedViewType(typeMapping, embeddableMapping);
            }

            embeddableTypeMap.put(embeddableMapping, t);
            return t;
        }
    }

    public Type<?> getKeyType(MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        if (embeddableMapping == null) {
            if (keyType != null) {
                return keyType;
            }
            if (keyViewMapping == null) {
                return keyType = context.getBasicType(viewMapping, declaredKeyType, declaredKeyTypeClass, getKeyTypes(getPossibleTargetTypes(context)));
            }
            return keyType = context.getManagedViewType(keyViewMapping, embeddableMapping);
        } else {
            if (embeddableKeyTypeMap == null) {
                embeddableKeyTypeMap = new HashMap<>(1);
            }
            Type<?> t = embeddableKeyTypeMap.get(embeddableMapping);
            if (t != null) {
                return t;
            }
            if (keyViewMapping == null) {
                t = context.getBasicType(viewMapping, declaredKeyType, declaredKeyTypeClass, getKeyTypes(getPossibleTargetTypes(context)));
            } else {
                t = context.getManagedViewType(keyViewMapping, embeddableMapping);
            }

            embeddableKeyTypeMap.put(embeddableMapping, t);
            return t;
        }
    }

    public Type<?> getElementType(MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        if (embeddableMapping == null) {
            if (elementType != null) {
                return elementType;
            }
            if (elementViewMapping == null) {
                return elementType = context.getBasicType(viewMapping, declaredElementType, declaredElementTypeClass, getElementTypes(getPossibleTargetTypes(context)));
            }
            return elementType = context.getManagedViewType(elementViewMapping, embeddableMapping);
        } else {
            if (embeddableElementTypeMap == null) {
                embeddableElementTypeMap = new HashMap<>(1);
            }
            Type<?> t = embeddableElementTypeMap.get(embeddableMapping);
            if (t != null) {
                return t;
            }
            if (elementViewMapping == null) {
                t = context.getBasicType(viewMapping, declaredElementType, declaredElementTypeClass, getElementTypes(getPossibleTargetTypes(context)));
            } else {
                t = context.getManagedViewType(elementViewMapping, embeddableMapping);
            }

            embeddableElementTypeMap.put(embeddableMapping, t);
            return t;
        }
    }

    public Map<ManagedViewTypeImplementor<?>, String> getInheritanceSubtypes(MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        if (embeddableMapping == null) {
            if (inheritanceSubtypes != null) {
                return inheritanceSubtypes;
            }
            return inheritanceSubtypes = initializeInheritanceSubtypes(inheritanceSubtypeMappings, typeMapping, context, embeddableMapping);
        } else {
            if (embeddableInheritanceSubtypesMap == null) {
                embeddableInheritanceSubtypesMap = new HashMap<>(1);
            }
            Map<ManagedViewTypeImplementor<?>, String> subtypes = embeddableInheritanceSubtypesMap.get(embeddableMapping);
            if (subtypes != null) {
                return subtypes;
            }

            subtypes = initializeInheritanceSubtypes(inheritanceSubtypeMappings, typeMapping, context, embeddableMapping);
            embeddableInheritanceSubtypesMap.put(embeddableMapping, subtypes);
            return subtypes;
        }
    }

    public Map<ManagedViewTypeImplementor<?>, String> getKeyInheritanceSubtypes(MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        if (embeddableMapping == null) {
            if (keyInheritanceSubtypes != null) {
                return keyInheritanceSubtypes;
            }
            return keyInheritanceSubtypes = initializeInheritanceSubtypes(keyInheritanceSubtypeMappings, keyViewMapping, context, embeddableMapping);
        } else {
            if (embeddableKeyInheritanceSubtypesMap == null) {
                embeddableKeyInheritanceSubtypesMap = new HashMap<>(1);
            }
            Map<ManagedViewTypeImplementor<?>, String> subtypes = embeddableKeyInheritanceSubtypesMap.get(embeddableMapping);
            if (subtypes != null) {
                return subtypes;
            }

            subtypes = initializeInheritanceSubtypes(keyInheritanceSubtypeMappings, keyViewMapping, context, embeddableMapping);
            embeddableKeyInheritanceSubtypesMap.put(embeddableMapping, subtypes);
            return subtypes;
        }
    }

    public Map<ManagedViewTypeImplementor<?>, String> getElementInheritanceSubtypes(MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        if (embeddableMapping == null) {
            if (elementInheritanceSubtypes != null) {
                return elementInheritanceSubtypes;
            }
            return elementInheritanceSubtypes = initializeInheritanceSubtypes(elementInheritanceSubtypeMappings, elementViewMapping, context, embeddableMapping);
        } else {
            if (embeddableElementInheritanceSubtypesMap == null) {
                embeddableElementInheritanceSubtypesMap = new HashMap<>(1);
            }
            Map<ManagedViewTypeImplementor<?>, String> subtypes = embeddableElementInheritanceSubtypesMap.get(embeddableMapping);
            if (subtypes != null) {
                return subtypes;
            }

            subtypes = initializeInheritanceSubtypes(elementInheritanceSubtypeMappings, elementViewMapping, context, embeddableMapping);
            embeddableElementInheritanceSubtypesMap.put(embeddableMapping, subtypes);
            return subtypes;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<ManagedViewTypeImplementor<?>, String> initializeInheritanceSubtypes(InheritanceViewMapping inheritanceSubtypeMappings, ViewMapping viewMapping, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        if (viewMapping == null || inheritanceSubtypeMappings == null || inheritanceSubtypeMappings.getInheritanceSubtypeMappings().isEmpty()) {
            return Collections.emptyMap();
        }
        Map<ManagedViewTypeImplementor<?>, String> map = new LinkedHashMap<>(inheritanceSubtypeMappings.getInheritanceSubtypeMappings().size());
        for (Map.Entry<ViewMapping, String> mappingEntry : inheritanceSubtypeMappings.getInheritanceSubtypeMappings().entrySet()) {
            String mapping = mappingEntry.getValue();
            if (mapping == null) {
                mapping = mappingEntry.getKey().determineInheritanceMapping(context);
                // An empty inheritance mapping signals that a subtype should actually be considered. If it was null it wouldn't be considered
                if (mapping == null) {
                    mapping = "";
                }
            }
            map.put(context.getManagedViewType(mappingEntry.getKey(), embeddableMapping), mapping);
        }
        return Collections.unmodifiableMap(map);
    }

    private ViewMapping getViewMapping(MetamodelBuildingContext context, java.lang.reflect.Type type, Class<?> classType) {
        TypeConverter<?, ?> typeConverter = null;
        boolean typeEntityView = context.isEntityView(classType);
        if (!typeEntityView) {
            // Try find a type converter for the declared type
            Map<Class<?>, ? extends TypeConverter<?, ?>> typeConverterMap = context.getTypeConverter(classType);
            if (!typeConverterMap.isEmpty()) {
                // Determine the entity model type
                for (ScalarTargetResolvingExpressionVisitor.TargetType targetType : getPossibleTargetTypes(context)) {
                    Class<?> entityModelType = targetType.getLeafBaseValueClass();

                    // Try find a converter match entity model type
                    typeConverter = typeConverterMap.get(entityModelType);
                    // Then try find a match for a "self" type
                    if (typeConverter == null) {
                        typeConverter = typeConverterMap.get(classType);
                    }
                    // Then try find a default
                    if (typeConverter == null) {
                        typeConverter = typeConverterMap.get(Object.class);
                    }
                    if (typeConverter != null) {
                        classType = typeConverter.getUnderlyingType(viewMapping.getEntityViewClass(), type);
                        typeEntityView = context.isEntityView(classType);
                        if (typeEntityView) {
                            break;
                        }
                    }
                }
            }
        }
        if (typeEntityView) {
            ViewMapping mapping = context.getViewMapping(classType);
            if (mapping == null) {
                unknownSubviewType(classType);
                return null;
            }
            mapping.initializeViewMappings(context, null);
            if (typeConverter == null) {
                return mapping;
            }

            return new ConvertedViewMapping(mapping, typeConverter, type);
        }

        return null;
    }

    public void initializeViewMappings(MetamodelBuildingContext context) {
        if (!resolvedTypeMappings) {
            // No need to check parameter attributes
            if (mapping.annotationType() == MappingParameter.class) {
                resolvedTypeMappings = true;
                return;
            }
            typeMapping = getViewMapping(context, declaredType, declaredTypeClass);
            if (typeMapping != null) {
                inheritanceSubtypeMappings = initializedInheritanceViewMappings(typeMapping, inheritanceSubtypeClassMappings, context);
            }
            keyViewMapping = getViewMapping(context, declaredKeyType, declaredKeyTypeClass);
            if (keyViewMapping != null) {
                keyInheritanceSubtypeMappings = initializedInheritanceViewMappings(keyViewMapping, keyInheritanceSubtypeClassMappings, context);
            }
            elementViewMapping = getViewMapping(context, declaredElementType, declaredElementTypeClass);
            if (elementViewMapping != null) {
                elementInheritanceSubtypeMappings = initializedInheritanceViewMappings(elementViewMapping, elementInheritanceSubtypeClassMappings, context);
            }
            resolvedTypeMappings = true;
        }
    }

    public boolean validateDependencies(MetamodelBuildingContext context, Set<Class<?>> dependencies, boolean reportError) {
        // No need to check parameter attributes
        if (mapping.annotationType() == MappingParameter.class) {
            return false;
        }
        boolean error = false;
        if (typeMapping != null) {
            if (typeMapping.validateDependencies(context, dependencies, this, null, reportError)) {
                if (reportError) {
                    error = true;
                    typeMapping = null;
                } else {
                    return true;
                }
            }
        }
        if (keyViewMapping != null) {
            if (keyViewMapping.validateDependencies(context, dependencies, this, null, reportError)) {
                if (reportError) {
                    error = true;
                    keyViewMapping = null;
                } else {
                    return true;
                }
            }
        }
        if (elementViewMapping != null) {
            if (elementViewMapping.validateDependencies(context, dependencies, this, null, reportError)) {
                if (reportError) {
                    error = true;
                    elementViewMapping = null;
                } else {
                    return true;
                }
            }
        }
        return error;
    }

    private InheritanceViewMapping initializedInheritanceViewMappings(ViewMapping attributeViewMapping, Map<Class<?>, String> inheritanceMapping, MetamodelBuildingContext context) {
        InheritanceViewMapping inheritanceViewMapping;
        Map<ViewMapping, String> subtypeMappings = new HashMap<>();
        if (attributeViewMapping != null) {
            if (inheritanceMapping == null) {
                inheritanceViewMapping = attributeViewMapping.getDefaultInheritanceViewMapping();
            } else {
                subtypeMappings = new HashMap<>(inheritanceMapping.size() + 1);

                for (Map.Entry<Class<?>, String> mappingEntry : inheritanceMapping.entrySet()) {
                    ViewMapping subtypeMapping = context.getViewMapping(mappingEntry.getKey());
                    if (subtypeMapping == null) {
                        unknownSubviewType(mappingEntry.getKey());
                    } else {
                        subtypeMapping.initializeViewMappings(context, null);
                        subtypeMappings.put(subtypeMapping, mappingEntry.getValue());
                    }
                }

                inheritanceViewMapping = new InheritanceViewMapping(subtypeMappings);
                attributeViewMapping.getInheritanceViewMappings().add(inheritanceViewMapping);
                return inheritanceViewMapping;
            }
        } else {
            inheritanceViewMapping = new InheritanceViewMapping(subtypeMappings);
        }

        return inheritanceViewMapping;
    }

    public void circularDependencyError(Set<Class<?>> dependencies) {
        context.addError("A circular dependency is introduced at the " + getErrorLocation() + " in the following dependency set: " + Arrays.deepToString(dependencies.toArray()));
    }

    public void circularDependencyDebug(ViewMapping viewMapping, Set<Class<?>> dependencies) {
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Removing allowed subtype '" + viewMapping.getEntityViewClass() + "' because of a possible circular dependency at the " + getErrorLocation() + " in the following dependency set: " + Arrays.deepToString(dependencies.toArray()));
        }
    }

    public void unknownSubviewType(Class<?> subviewClass) {
        context.addError("An unknown or unregistered subview type '" + subviewClass.getName() + "' is used at the " + getErrorLocation() + "!");
    }
}
