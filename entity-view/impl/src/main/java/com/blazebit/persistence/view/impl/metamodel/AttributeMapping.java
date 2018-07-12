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

import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.impl.ScalarTargetResolvingExpressionVisitor;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.spi.EntityViewAttributeMapping;
import com.blazebit.persistence.view.spi.type.TypeConverter;

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

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AttributeMapping implements EntityViewAttributeMapping {

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

    protected InheritanceViewMapping inheritanceSubtypeMappings;
    protected InheritanceViewMapping keyInheritanceSubtypeMappings;
    protected InheritanceViewMapping elementInheritanceSubtypeMappings;
    protected Map<ManagedViewTypeImplementor<?>, String> inheritanceSubtypes;
    protected Map<ManagedViewTypeImplementor<?>, String> keyInheritanceSubtypes;
    protected Map<ManagedViewTypeImplementor<?>, String> elementInheritanceSubtypes;

    protected AbstractAttribute<?, ?> attribute;

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

    public abstract String determineMappedBy(ManagedType<?> managedType, String mapping, MetamodelBuildingContext context);

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

    public Class<?> getJavaType(MetamodelBuildingContext context) {
        Type<?> t = getType(context);
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

    public Type<?> getType(MetamodelBuildingContext context) {
        if (type != null) {
            return type;
        }
        if (typeMapping == null) {
            return type = context.getBasicType(viewMapping, declaredType, declaredTypeClass, getBaseTypes(getPossibleTargetTypes(context)));
        }
        return type = typeMapping.getManagedViewType(context);
    }

    public Type<?> getKeyType(MetamodelBuildingContext context) {
        if (keyType != null) {
            return keyType;
        }
        if (keyViewMapping == null) {
            return keyType = context.getBasicType(viewMapping, declaredKeyType, declaredKeyTypeClass, getKeyTypes(getPossibleTargetTypes(context)));
        }
        return keyType = keyViewMapping.getManagedViewType(context);
    }

    public Type<?> getElementType(MetamodelBuildingContext context) {
        if (elementType != null) {
            return elementType;
        }
        if (elementViewMapping == null) {
            return elementType = context.getBasicType(viewMapping, declaredElementType, declaredElementTypeClass, getElementTypes(getPossibleTargetTypes(context)));
        }
        return elementType = elementViewMapping.getManagedViewType(context);
    }

    public Map<ManagedViewTypeImplementor<?>, String> getInheritanceSubtypes(MetamodelBuildingContext context) {
        if (inheritanceSubtypes != null) {
            return inheritanceSubtypes;
        }
        return inheritanceSubtypes = initializeInheritanceSubtypes(inheritanceSubtypeMappings, typeMapping, context);
    }

    public Map<ManagedViewTypeImplementor<?>, String> getKeyInheritanceSubtypes(MetamodelBuildingContext context) {
        if (keyInheritanceSubtypes != null) {
            return keyInheritanceSubtypes;
        }
        return keyInheritanceSubtypes = initializeInheritanceSubtypes(keyInheritanceSubtypeMappings, keyViewMapping, context);
    }

    public Map<ManagedViewTypeImplementor<?>, String> getElementInheritanceSubtypes(MetamodelBuildingContext context) {
        if (elementInheritanceSubtypes != null) {
            return elementInheritanceSubtypes;
        }
        return elementInheritanceSubtypes = initializeInheritanceSubtypes(elementInheritanceSubtypeMappings, elementViewMapping, context);
    }

    @SuppressWarnings("unchecked")
    private Map<ManagedViewTypeImplementor<?>, String> initializeInheritanceSubtypes(InheritanceViewMapping inheritanceSubtypeMappings, ViewMapping viewMapping, MetamodelBuildingContext context) {
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
            map.put(mappingEntry.getKey().getManagedViewType(context), mapping);
        }
        if (map.equals(viewMapping.getManagedViewType(context).getInheritanceSubtypeConfiguration())) {
            return (Map<ManagedViewTypeImplementor<?>, String>) (Map<?, ?>) viewMapping.getManagedViewType(context).getInheritanceSubtypeConfiguration();
        } else {
            return Collections.unmodifiableMap(map);
        }
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

    public void unknownSubviewType(Class<?> subviewClass) {
        context.addError("An unknown or unregistered subview type '" + subviewClass.getName() + "' is used at the " + getErrorLocation() + "!");
    }
}
