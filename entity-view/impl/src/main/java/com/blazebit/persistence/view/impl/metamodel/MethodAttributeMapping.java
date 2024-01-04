/*
 * Copyright 2014 - 2024 Blazebit.
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

import com.blazebit.persistence.spi.AttributePath;
import com.blazebit.persistence.view.AttributeFilterProvider;
import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.MappingIndex;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.impl.ReflectionUtils;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedMethodCollectionAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedMethodListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedMethodMapAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedMethodMultiListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedMethodMultiMapAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedMethodSetAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedMethodSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingMethodCollectionAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingMethodListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingMethodMapAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingMethodMultiListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingMethodMultiMapAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingMethodSetAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingMethodSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.SubqueryMethodSingularAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.spi.EntityViewMethodAttributeMapping;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Type;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MethodAttributeMapping extends AttributeMapping implements EntityViewMethodAttributeMapping {

    private final String attributeName;
    private final Method method;
    private final Method setterMethod;
    private final int attributeIndex;

    private Map<String, Class<? extends AttributeFilterProvider<?>>> attributeFilterProviders;

    // Updatable configs
    private Boolean isUpdatable;
    private Boolean isOrphanRemoval;
    private Boolean isOptimisticLockProtected;
    private String mappedBy;
    private Map<EmbeddableOwner, String> embeddableMappedByMap;
    private InverseRemoveStrategy inverseRemoveStrategy;
    private Set<CascadeType> cascadeTypes = Collections.singleton(CascadeType.AUTO);
    private Set<Class<?>> cascadeSubtypeClasses;
    private Set<Class<?>> cascadePersistSubtypeClasses;
    private Set<Class<?>> cascadeUpdateSubtypeClasses;

    private Map<ViewMapping, Boolean> readOnlySubtypeMappings;
    private Map<ViewMapping, Boolean> cascadeSubtypeMappings;
    private Map<ViewMapping, Boolean> cascadePersistSubtypeMappings;
    private Map<ViewMapping, Boolean> cascadeUpdateSubtypeMappings;
    private Set<ManagedViewTypeImplementor<?>> readOnlySubtypes;
    private Set<ManagedViewTypeImplementor<?>> cascadeSubtypes;
    private Set<ManagedViewTypeImplementor<?>> cascadePersistSubtypes;
    private Set<ManagedViewTypeImplementor<?>> cascadeUpdateSubtypes;
    private Map<EmbeddableOwner, Set<ManagedViewTypeImplementor<?>>> embeddableReadOnlySubtypesMap;
    private Map<EmbeddableOwner, Set<ManagedViewTypeImplementor<?>>> embeddableCascadeSubtypesMap;
    private Map<EmbeddableOwner, Set<ManagedViewTypeImplementor<?>>> embeddableCascadePersistSubtypesMap;
    private Map<EmbeddableOwner, Set<ManagedViewTypeImplementor<?>>> embeddableCascadeUpdateSubtypesMap;

    public MethodAttributeMapping(ViewMapping viewMapping, Annotation mapping, MappingIndex mappingIndex, MetamodelBootContext context, String attributeName, Method method, int attributeIndex, boolean isCollection, PluralAttribute.ElementCollectionType elementCollectionType, Class<?> declaredTypeClass, Class<?> declaredKeyTypeClass, Class declaredElementTypeClass,
                                  java.lang.reflect.Type type, java.lang.reflect.Type keyType, java.lang.reflect.Type elementType, Map<Class<?>, String> inheritanceSubtypeClassMappings, Map<Class<?>, String> keyInheritanceSubtypeClassMappings, Map<Class<?>, String> elementInheritanceSubtypeClassMappings) {
        super(viewMapping, mapping, mappingIndex, context, isCollection, elementCollectionType, declaredTypeClass, declaredKeyTypeClass, declaredElementTypeClass, type, keyType, elementType, inheritanceSubtypeClassMappings, keyInheritanceSubtypeClassMappings, elementInheritanceSubtypeClassMappings);
        this.attributeName = attributeName;
        this.method = method;
        this.attributeIndex = attributeIndex;
        Method setter = null;
        Method covariantGetter = null;
        List<Method> setters = ReflectionUtils.getSetters(viewMapping.getEntityViewClass(), attributeName);
        List<Method> illegalSetters = null;
        for (Method m : setters) {
            if ((m.getModifiers() & Modifier.ABSTRACT) != 0) {
                if (com.blazebit.reflection.ReflectionUtils.getResolvedMethodParameterTypes(viewMapping.getEntityViewClass(), m)[0].equals(com.blazebit.reflection.ReflectionUtils.getResolvedMethodReturnType(viewMapping.getEntityViewClass(), method))) {
                    setter = m;
                    break;
                } else {
                    // Before considering this illegal, let's consider that the getter could be covariant overridden
                    if (covariantGetter == null) {
                        covariantGetter = com.blazebit.reflection.ReflectionUtils.getMethod(viewMapping.getEntityViewClass(), method.getName());
                    }
                    if (com.blazebit.reflection.ReflectionUtils.getResolvedMethodParameterTypes(viewMapping.getEntityViewClass(), m)[0].equals(com.blazebit.reflection.ReflectionUtils.getResolvedMethodReturnType(viewMapping.getEntityViewClass(), covariantGetter))) {
                        setter = m;
                        break;
                    }
                    if (illegalSetters == null) {
                        illegalSetters = new ArrayList<>();
                    }
                    illegalSetters.add(m);
                }
            }
        }
        if (setter == null && illegalSetters != null) {
            for (Method m : illegalSetters) {
                context.addError("The getter '" + method.getName() + "' of the class '" + viewMapping.getEntityViewClass().getName()
                        + "' must have the same return type as it's corresponding setter accepts, but found " + m.getParameterTypes()[0].getName());
            }

        }
        this.setterMethod = setter;
    }

    @Override
    public ViewMapping getDeclaringView() {
        return viewMapping;
    }

    @Override
    public String getName() {
        return attributeName;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    public Method getSetterMethod() {
        return setterMethod;
    }

    public int getAttributeIndex() {
        return attributeIndex;
    }

    @Override
    public Boolean getUpdatable() {
        return isUpdatable;
    }

    @Override
    public Boolean getOrphanRemoval() {
        return isOrphanRemoval;
    }

    public Boolean getOptimisticLockProtected() {
        return isOptimisticLockProtected;
    }

    @Override
    public boolean isId() {
        return viewMapping.getIdAttribute() == this;
    }

    @Override
    public boolean isVersion() {
        return viewMapping.getVersionAttribute() == this;
    }

    @Override
    public Set<CascadeType> getCascadeTypes() {
        return cascadeTypes;
    }

    @Override
    public void setUpdatable(boolean updatable, boolean orphanRemoval, CascadeType[] cascadeTypes, Class<?>[] subtypes, Class<?>[] persistSubtypes, Class<?>[] updateSubtypes) {
        this.isUpdatable = updatable;
        this.isOrphanRemoval = orphanRemoval;
        this.cascadeTypes = new HashSet<>(Arrays.asList(cascadeTypes));
        this.cascadeSubtypeClasses = new HashSet<>(Arrays.asList(subtypes));
        this.cascadePersistSubtypeClasses = new HashSet<>(Arrays.asList(persistSubtypes));
        this.cascadeUpdateSubtypeClasses = new HashSet<>(Arrays.asList(updateSubtypes));
    }

    public void setOptimisticLockProtected(Boolean optimisticLockProtected) {
        isOptimisticLockProtected = optimisticLockProtected;
    }

    @Override
    public String getMappedBy() {
        return mappedBy;
    }

    @Override
    public void setMappedBy(String mappedBy) {
        this.mappedBy = mappedBy;
    }

    @Override
    public InverseRemoveStrategy getInverseRemoveStrategy() {
        return inverseRemoveStrategy;
    }

    @Override
    public void setInverseRemoveStrategy(InverseRemoveStrategy inverseRemoveStrategy) {
        if (inverseRemoveStrategy == null) {
            throw new IllegalArgumentException("Invalid null remove strategy!");
        }
        this.inverseRemoveStrategy = inverseRemoveStrategy;
    }

    @Override
    public Map<String, Class<? extends AttributeFilterProvider<?>>> getAttributeFilterProviders() {
        return attributeFilterProviders;
    }

    @Override
    public void setAttributeFilterProviders(Map<String, Class<? extends AttributeFilterProvider<?>>> attributeFilterProviders) {
        this.attributeFilterProviders = attributeFilterProviders;
    }

    public Set<ManagedViewTypeImplementor<?>> getReadOnlySubtypes(MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        Set<ManagedViewTypeImplementor<?>> readOnlySubtypes;
        if (embeddableMapping == null) {
            readOnlySubtypes = this.readOnlySubtypes;
        } else {
            if (embeddableReadOnlySubtypesMap == null) {
                embeddableReadOnlySubtypesMap = new HashMap<>(1);
            }
            readOnlySubtypes = embeddableReadOnlySubtypesMap.get(embeddableMapping);
        }
        if (readOnlySubtypes != null) {
            return readOnlySubtypes;
        }
        com.blazebit.persistence.view.metamodel.Type<?> elementType = getElementType(context, embeddableMapping);
        if (elementType == null) {
            elementType = getType(context, embeddableMapping);
        }
        Set<ManagedViewTypeImplementor<?>> subtypes;
        if (readOnlySubtypeMappings == null || readOnlySubtypeMappings.isEmpty()) {
            if (elementType instanceof ManagedViewTypeImplementor<?>) {
                subtypes = Collections.<ManagedViewTypeImplementor<?>>singleton((ManagedViewTypeImplementor<?>) elementType);
            } else {
                subtypes = Collections.emptySet();
            }
        } else {
            subtypes = initializeReadOnlyCascadeSubtypes(readOnlySubtypeMappings, context, embeddableMapping);
            if (elementType instanceof ManagedViewTypeImplementor<?>) {
                subtypes.add((ManagedViewTypeImplementor<?>) elementType);
            }
        }
        if (embeddableMapping == null) {
            this.readOnlySubtypes = subtypes;
        } else {
            embeddableReadOnlySubtypesMap.put(embeddableMapping, subtypes);
        }

        return subtypes;
    }

    public Set<ManagedViewTypeImplementor<?>> getCascadeSubtypes(MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        if (embeddableMapping == null) {
            if (cascadeSubtypes != null) {
                return cascadeSubtypes;
            }
            return cascadeSubtypes = initializeCascadeSubtypes(cascadeSubtypeMappings, context, embeddableMapping);
        } else {
            if (embeddableCascadeSubtypesMap == null) {
                embeddableCascadeSubtypesMap = new HashMap<>(1);
            }
            Set<ManagedViewTypeImplementor<?>> cascadeSubtypes = embeddableCascadeSubtypesMap.get(embeddableMapping);
            if (cascadeSubtypes != null) {
                return cascadeSubtypes;
            }
            cascadeSubtypes = initializeCascadeSubtypes(cascadeSubtypeMappings, context, embeddableMapping);
            embeddableCascadeSubtypesMap.put(embeddableMapping, cascadeSubtypes);
            return cascadeSubtypes;
        }
    }

    public Set<ManagedViewTypeImplementor<?>> getCascadePersistSubtypes(MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        if (embeddableMapping == null) {
            if (cascadePersistSubtypes != null) {
                return cascadePersistSubtypes;
            }
            return cascadePersistSubtypes = initializeCascadeSubtypes(cascadePersistSubtypeMappings, context, embeddableMapping);
        } else {
            if (embeddableCascadePersistSubtypesMap == null) {
                embeddableCascadePersistSubtypesMap = new HashMap<>(1);
            }
            Set<ManagedViewTypeImplementor<?>> cascadePersistSubtypes = embeddableCascadePersistSubtypesMap.get(embeddableMapping);
            if (cascadePersistSubtypes != null) {
                return cascadePersistSubtypes;
            }
            cascadePersistSubtypes = initializeCascadeSubtypes(cascadePersistSubtypeMappings, context, embeddableMapping);
            embeddableCascadePersistSubtypesMap.put(embeddableMapping, cascadePersistSubtypes);
            return cascadePersistSubtypes;
        }
    }

    public Set<ManagedViewTypeImplementor<?>> getCascadeUpdateSubtypes(MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        if (embeddableMapping == null) {
            if (cascadeUpdateSubtypes != null) {
                return cascadeUpdateSubtypes;
            }
            return cascadeUpdateSubtypes = initializeCascadeSubtypes(cascadeUpdateSubtypeMappings, context, embeddableMapping);
        } else {
            if (embeddableCascadeUpdateSubtypesMap == null) {
                embeddableCascadeUpdateSubtypesMap = new HashMap<>(1);
            }
            Set<ManagedViewTypeImplementor<?>> cascadeUpdateSubtypes = embeddableCascadeUpdateSubtypesMap.get(embeddableMapping);
            if (cascadeUpdateSubtypes != null) {
                return cascadeUpdateSubtypes;
            }
            cascadeUpdateSubtypes = initializeCascadeSubtypes(cascadeUpdateSubtypeMappings, context, embeddableMapping);
            embeddableCascadeUpdateSubtypesMap.put(embeddableMapping, cascadeUpdateSubtypes);
            return cascadeUpdateSubtypes;
        }
    }

    private Set<ManagedViewTypeImplementor<?>> initializeReadOnlyCascadeSubtypes(Map<ViewMapping, Boolean> subtypeMappings, final MetamodelBuildingContext context, final EmbeddableOwner embeddableMapping) {
        if (subtypeMappings == null || subtypeMappings.isEmpty()) {
            return Collections.emptySet();
        }
        final Set<ManagedViewTypeImplementor<?>> subtypes = Collections.newSetFromMap(new ConcurrentHashMap<ManagedViewTypeImplementor<?>, Boolean>(subtypeMappings.size()));
        for (final ViewMapping mapping : subtypeMappings.keySet()) {
            mapping.onInitializeViewMappingsFinished(new Runnable() {
                @Override
                public void run() {
                    subtypes.add(context.getManagedViewType(mapping, embeddableMapping));
                }
            });
        }
        return subtypes;
    }

    private Set<ManagedViewTypeImplementor<?>> initializeCascadeSubtypes(Map<ViewMapping, Boolean> subtypeMappings, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        if (subtypeMappings == null || subtypeMappings.isEmpty()) {
            return Collections.emptySet();
        }
        Set<ManagedViewTypeImplementor<?>> subtypes = new HashSet<>(subtypeMappings.size());
        for (ViewMapping mapping : subtypeMappings.keySet()) {
            subtypes.add(context.getManagedViewType(mapping, embeddableMapping));
        }
        return subtypes;
    }

    @Override
    public String getErrorLocation() {
        return getLocation(attributeName, method);
    }

    public static String getLocation(String attributeName, Method method) {
        return "attribute " + attributeName + "[" + methodReference(method) + "]";
    }

    public boolean hasExplicitCascades() {
        return !isEmpty(cascadeSubtypeClasses) || !isEmpty(cascadePersistSubtypeClasses) || !isEmpty(cascadeUpdateSubtypeClasses);
    }

    @Override
    public void initializeViewMappings(MetamodelBuildingContext context) {
        super.initializeViewMappings(context);
        // No need to check parameter attributes
        if (mapping.annotationType() == MappingParameter.class) {
            return;
        }

        if (isEmpty(cascadeSubtypeClasses) && isEmpty(cascadePersistSubtypeClasses) && isEmpty(cascadeUpdateSubtypeClasses)) {
            // If no classes are given, we try to find all subtype classes
            boolean isCollection = false;
            ViewMapping attributeViewMapping;
            if (elementViewMapping == null) {
                attributeViewMapping = typeMapping;
            } else {
                attributeViewMapping = elementViewMapping;
                isCollection = true;
            }
            this.readOnlySubtypeMappings = Collections.emptyMap();
            this.cascadeSubtypeMappings = Collections.emptyMap();
            this.cascadePersistSubtypeMappings = Collections.emptyMap();
            this.cascadeUpdateSubtypeMappings = Collections.emptyMap();
            if (attributeViewMapping != null) {
                // For now, we treat flat views as if they had support for multiple parents as they are most likely only used once
                // When we add support for multiple parents at some point, we would just have to switch to true here
                ManagedType<?> managedType = attributeViewMapping.getManagedType(context);
                boolean allowMultiParent = managedType != null && managedType.getPersistenceType() == Type.PersistenceType.EMBEDDABLE;
                // We also allow creatable/updatable instances if cascade uses AUTO, there is a setter and multiple-parents are allowed
                boolean allowCreatable = attributeViewMapping.isCreatable(context) || cascadeTypes.contains(CascadeType.PERSIST) || cascadeTypes.contains(CascadeType.AUTO) && setterMethod != null && allowMultiParent;
                if (isUpdatable != Boolean.FALSE && getDeclaringView().isUpdatable()) {
                    boolean allowUpdatable = attributeViewMapping.isUpdatable() || cascadeTypes.contains(CascadeType.UPDATE) || cascadeTypes.contains(CascadeType.AUTO) && setterMethod != null && allowMultiParent;

                    if (setterMethod != null || isCollection) {
                        // When the attribute is updatable, we don't allow updatable or creatable types as read only types
                        this.readOnlySubtypeMappings = initializeDependentSubtypeMappingsAuto(context, attributeViewMapping.getEntityViewClass(), true, !allowUpdatable, !allowCreatable, false);
                    }
                    if (setterMethod != null || isCollection && allowCreatable) {
                        // But only if the attribute is explicitly or implicitly updatable
                        this.cascadeSubtypeMappings = initializeDependentSubtypeMappingsAuto(context, attributeViewMapping.getEntityViewClass(), true, allowUpdatable, allowCreatable, true);
                    }
                } else {
                    // Allow all read-only subtypes and also creatable subtypes for creatable-only views
                    if (getDeclaringView().isCreatable(context)) {
                        if (setterMethod != null || isCollection) {
                            this.readOnlySubtypeMappings = initializeDependentSubtypeMappingsAuto(context, attributeViewMapping.getEntityViewClass(), true, true, !allowCreatable, false);
                        }
                        if (setterMethod != null || isCollection && allowCreatable) {
                            this.cascadePersistSubtypeMappings = initializeDependentSubtypeMappingsAuto(context, attributeViewMapping.getEntityViewClass(), false, false, allowCreatable, true);
                        }
                    }
                }
            }
        } else if (isUpdatable == Boolean.TRUE) {
            this.readOnlySubtypeMappings = initializeDependentSubtypeMappings(context, cascadeSubtypeClasses, true);
            this.cascadeSubtypeMappings = initializeDependentSubtypeMappings(context, cascadeSubtypeClasses, false);
            this.cascadePersistSubtypeMappings = initializeDependentSubtypeMappings(context, cascadePersistSubtypeClasses, false);
            this.cascadeUpdateSubtypeMappings = initializeDependentSubtypeMappings(context, cascadeUpdateSubtypeClasses, false);
        }
    }

    private static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    @Override
    public boolean validateDependencies(MetamodelBuildingContext context, Set<Class<?>> dependencies, boolean reportError) {
        // No need to check parameter attributes
        if (mapping.annotationType() == MappingParameter.class) {
            return false;
        }
        boolean error = super.validateDependencies(context, dependencies, reportError);
        if (error && !reportError) {
            return true;
        }

        error |= validateCascadeSubtypeMappings(context, dependencies, cascadeSubtypeMappings, reportError);
        if (error && !reportError) {
            return true;
        }
        error |= validateCascadeSubtypeMappings(context, dependencies, cascadePersistSubtypeMappings, reportError);
        if (error && !reportError) {
            return true;
        }
        error |= validateCascadeSubtypeMappings(context, dependencies, cascadeUpdateSubtypeMappings, reportError);
        if (error && !reportError) {
            return true;
        }

        return error;
    }

    @Override
    public boolean determineDisallowOwnedUpdatableSubview(MetamodelBuildingContext context, EmbeddableOwner embeddableMapping, Attribute<?, ?> updateMappableAttribute) {
        if (disallowOwnedUpdatableSubview != null) {
            return disallowOwnedUpdatableSubview;
        }
        String mappedBy;
        if (embeddableMapping == null) {
            mappedBy = this.mappedBy;
        } else {
            if (embeddableMappedByMap == null) {
                embeddableMappedByMap = new HashMap<>(1);
            }
            mappedBy = embeddableMappedByMap.get(embeddableMapping);
        }

        return disallowOwnedUpdatableSubview = isCollection || mappedBy != null && !mappedBy.isEmpty() || updateMappableAttribute != null && updateMappableAttribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE;
    }

    public String determineMappedBy(ManagedType<?> managedType, String mapping, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        String mappedBy;
        if (embeddableMapping == null) {
            mappedBy = this.mappedBy;
        } else {
            if (embeddableMappedByMap == null) {
                embeddableMappedByMap = new HashMap<>(1);
            }
            mappedBy = embeddableMappedByMap.get(embeddableMapping);
        }
        if (mappedBy != null) {
            if (mappedBy.isEmpty()) {
                return null;
            }
            return mappedBy;
        }

        if (embeddableMapping == null) {
            this.mappedBy = "";
        } else {
            embeddableMappedByMap.put(embeddableMapping, "");
        }

        if (mapping == null || mapping.isEmpty()) {
            return null;
        }
        if (!(managedType instanceof EntityType<?>)) {
            // Can't determine the inverse mapped by attribute of a non-entity
            return null;
        }

        try {
            AttributePath basicAttributePath = context.getJpaProvider().getJpaMetamodelAccessor().getAttributePath(context.getEntityMetamodel(), managedType, mapping);
            List<Attribute<?, ?>> attributes = basicAttributePath.getAttributes();
            for (int i = 1; i < attributes.size(); i++) {
                if (attributes.get(i).getDeclaringType().getPersistenceType() != Type.PersistenceType.EMBEDDABLE) {
                    // If the mapping goes over a non-embeddable, we can't determine a mapped by attribute name
                    return null;
                }
            }
            mappedBy = context.getJpaProvider().getMappedBy((EntityType<?>) managedType, mapping);
            if (embeddableMapping == null) {
                this.mappedBy = mappedBy;
            } else {
                embeddableMappedByMap.put(embeddableMapping, mappedBy);
            }
            return mappedBy;
        } catch (IllegalArgumentException ex) {
            // if the mapping is invalid, we skip the determination as the error will be analyzed further at a later stage
            return null;
        }
    }

    public Map<String, String> determineWritableMappedByMappings(ManagedType<?> managedType, String mappedBy, MetamodelBuildingContext context) {
        ViewMapping elementViewMapping = getElementViewMapping();
        EntityType<?> elementType;
        if (elementViewMapping != null) {
            elementType = context.getEntityMetamodel().getEntity(elementViewMapping.getEntityClass());
        } else {
            Class<?> declaredElementType = getDeclaredElementType();
            if (declaredElementType != null) {
                elementType = context.getEntityMetamodel().getEntity(declaredElementType);
            } else {
                elementType = context.getEntityMetamodel().getEntity(getDeclaredType());
            }
        }
        if (elementType == null) {
            return null;
        }

        EntityType<?> entityType = (EntityType<?>) managedType;
        try {
            Map<String, String> writableMappedByMappings = context.getJpaProvider().getWritableMappedByMappings(entityType, elementType, mappedBy, ((Mapping) mapping).value());
            if (writableMappedByMappings == null) {
                return null;
            } else {
                return Collections.unmodifiableMap(writableMappedByMappings);
            }
        } catch (RuntimeException ex) {
            // Graceful error message for cases when an inverse attribute was mapped on a wrong type
            context.addError("Couldn't determine writable mappings for the mapped by mapping '" + mappedBy + "' on the entity '" + elementType.getName() + "' declared by the entity '" + entityType.getName() + "' through a entity view mapping at the " + getErrorLocation());
            return null;
        }
    }

    private boolean validateCascadeSubtypeMappings(MetamodelBuildingContext context, Set<Class<?>> dependencies, Map<ViewMapping, Boolean> mappings, boolean reportError) {
        if (mappings == null || mappings.isEmpty()) {
            return false;
        }
        boolean error = false;
        Iterator<Map.Entry<ViewMapping, Boolean>> iterator = mappings.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ViewMapping, Boolean> entry = iterator.next();
            ViewMapping mapping = entry.getKey();
            // Only report errors if this is an explicit mapping i.e. entry.getValue() == Boolean.TRUE
            if (mapping.validateDependencies(context, dependencies, this, null, entry.getValue() == Boolean.TRUE)) {
                iterator.remove();
                // This is only an error if a mapping was explicit
                if (entry.getValue() == Boolean.TRUE) {
                    error = true;
                    if (!reportError) {
                        return true;
                    }
                }
            }
        }
        return error;
    }

    private Map<ViewMapping, Boolean> initializeDependentSubtypeMappings(MetamodelBuildingContext context, Set<Class<?>> subtypes, boolean readOnly) {
        if (subtypes.size() == 0) {
            return Collections.emptyMap();
        }

        Map<ViewMapping, Boolean> subtypeMappings = new HashMap<>(subtypes.size());
        for (Class<?> type : subtypes) {
            ViewMapping subtypeMapping = context.getViewMapping(type);
            if (subtypeMapping == null) {
                unknownSubviewType(type);
            } else if (!readOnly || !subtypeMapping.isUpdatable() && !subtypeMapping.isCreatable(context)) {
                subtypeMapping.initializeViewMappings(context, null);
                subtypeMappings.put(subtypeMapping, Boolean.TRUE);
            }
        }

        return subtypeMappings;
    }

    private Map<ViewMapping, Boolean> initializeDependentSubtypeMappingsAuto(final MetamodelBuildingContext context, final Class<?> clazz, boolean allowReadOnly, boolean allowUpdatable, boolean allowCreatable, final boolean removeCycles) {
        Set<Class<?>> subtypes = context.findSubtypes(clazz);
        if (subtypes.size() == 0) {
            return Collections.emptyMap();
        }

        final Map<ViewMapping, Boolean> subtypeMappings = new HashMap<>(subtypes.size());
        for (final Class<?> type : subtypes) {
            final ViewMapping subtypeMapping = context.getViewMapping(type);
            if (subtypeMapping == null) {
                unknownSubviewType(type);
            } else if (allowSubtype(context, subtypeMapping, allowReadOnly, allowUpdatable, allowCreatable)) {
                // We can't initialize a potential subtype mapping here immediately, but have to wait until the current view mapping is fully initialized
                // This avoids access to partly initialized view mappings
                viewMapping.onInitializeViewMappingsFinished(new Runnable() {
                    @Override
                    public void run() {
                        subtypeMapping.onInitializeViewMappingsFinished(new Runnable() {
                            @Override
                            public void run() {
                                Set<Class<?>> dependencies = new HashSet<>();
                                dependencies.add(MethodAttributeMapping.this.getDeclaringView().getEntityViewClass());
                                dependencies.add(clazz);
                                if (!removeCycles || !subtypeMapping.validateDependencies(context, dependencies, MethodAttributeMapping.this, clazz, false)) {
                                    // validateCascadeSubtypeMappings will remove this when this speculation was invalid
                                    subtypeMappings.put(subtypeMapping, Boolean.FALSE);
                                }
                            }
                        });
                    }
                });
            }
        }

        return subtypeMappings;
    }

    private static boolean allowSubtype(MetamodelBuildingContext context, ViewMapping subtypeMapping, boolean allowReadOnly, boolean allowUpdatable, boolean allowCreatable) {
        if (allowUpdatable && subtypeMapping.isUpdatable() && (allowCreatable == subtypeMapping.isCreatable(context))) {
            return true;
        } else if (allowCreatable && subtypeMapping.isCreatable(context)) {
            return true;
        } else {
            return allowReadOnly && !subtypeMapping.isUpdatable() && !subtypeMapping.isCreatable(context);
        }
    }

    public MethodAttributeMapping handleReplacement(AttributeMapping original) {
        if (original == null) {
            return this;
        }
        if (!(original instanceof MethodAttributeMapping)) {
            throw new IllegalStateException("Tried to replace attribute [" + original + "] with method attribute: " + this);
        }

        MethodAttributeMapping originalAttribute = (MethodAttributeMapping) original;
        // If the mapping is the same, just let it through
        if (mapping.equals(originalAttribute.getMapping()) && Objects.equals(mappingIndex, originalAttribute.getMappingIndex())) {
            // But only if the method is more concrete
            if (method.getDeclaringClass().isInterface()) {
                return originalAttribute;
            }
            if (originalAttribute.getMethod().getDeclaringClass().isInterface()) {
                return this;
            }
        }

        // Also let through the attributes that are "specialized" in subclasses
        if (method.getDeclaringClass() != originalAttribute.getMethod().getDeclaringClass()
                && method.getDeclaringClass().isAssignableFrom(originalAttribute.getMethod().getDeclaringClass())) {
            // The method is overridden/specialized by the method of the existing attribute
            return originalAttribute;
        }

        // If the original is implicitly mapped, but this attribute isn't, we have to replace it
        if (originalAttribute.getMapping() instanceof MappingLiteral) {
            return this;
        }

        context.addError("Conflicting attribute mapping or index mapping for attribute '" + attributeName + "' at the methods [" + methodReference(method) + ", " + methodReference(originalAttribute.getMethod()) + "] for managed view type '" + viewMapping.getEntityViewClass().getName() + "'");
        return originalAttribute;
    }

    private static String methodReference(Method method) {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    // If you change something here don't forget to also update ParameterAttributeMapping#getParameterAttribute
    @SuppressWarnings("unchecked")
    public <X> AbstractMethodAttribute<? super X, ?> getMethodAttribute(ManagedViewTypeImplementor<X> viewType, int attributeIndex, int dirtyStateIndex, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        AbstractAttribute<?, ?> attribute;
        if (embeddableMapping == null) {
            attribute = this.attribute;
        } else {
            if (embeddableAttributeMap == null) {
                embeddableAttributeMap = new HashMap<>(1);
            }
            attribute = embeddableAttributeMap.get(embeddableMapping);
        }
        if (attribute == null) {
            if (this.attributeIndex != -1) {
                attributeIndex = this.attributeIndex;
            }
            if (mapping instanceof MappingParameter) {
                if (embeddableMapping == null) {
                    mappedBy = "";
                } else {
                    if (embeddableMappedByMap == null) {
                        embeddableMappedByMap = new HashMap<>(1);
                    }
                    embeddableMappedByMap.put(embeddableMapping, "");
                }
                attribute = new MappingMethodSingularAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex, embeddableMapping);
                return (AbstractMethodAttribute<? super X, ?>) attribute;
            }

            boolean correlated = mapping instanceof MappingCorrelated || mapping instanceof MappingCorrelatedSimple;

            if (isCollection) {
                if (Collection.class == declaredTypeClass) {
                    if (correlated) {
                        attribute = new CorrelatedMethodCollectionAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex, embeddableMapping);
                    } else {
                        attribute = new MappingMethodCollectionAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex, embeddableMapping);
                    }
                } else if (List.class == declaredTypeClass) {
                    if (elementCollectionType == null) {
                        if (correlated) {
                            attribute = new CorrelatedMethodListAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex, embeddableMapping);
                        } else {
                            attribute = new MappingMethodListAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex, embeddableMapping);
                        }
                    } else {
                        if (correlated) {
                            attribute = new CorrelatedMethodMultiListAttribute<X, Object, Collection<Object>>(viewType, this, context, attributeIndex, dirtyStateIndex, embeddableMapping);
                        } else {
                            attribute = new MappingMethodMultiListAttribute<X, Object, Collection<Object>>(viewType, this, context, attributeIndex, dirtyStateIndex, embeddableMapping);
                        }
                    }
                } else if (Set.class == declaredTypeClass || SortedSet.class == declaredTypeClass || NavigableSet.class == declaredTypeClass) {
                    if (correlated) {
                        attribute = new CorrelatedMethodSetAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex, embeddableMapping);
                    } else {
                        attribute = new MappingMethodSetAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex, embeddableMapping);
                    }
                } else if (Map.class == declaredTypeClass || SortedMap.class == declaredTypeClass || NavigableMap.class == declaredTypeClass) {
                    if (elementCollectionType == null) {
                        if (correlated) {
                            attribute = new CorrelatedMethodMapAttribute<X, Object, Object>(viewType, this, context, attributeIndex, dirtyStateIndex, embeddableMapping);
                        } else {
                            attribute = new MappingMethodMapAttribute<X, Object, Object>(viewType, this, context, attributeIndex, dirtyStateIndex, embeddableMapping);
                        }
                    } else {
                        if (correlated) {
                            attribute = new CorrelatedMethodMultiMapAttribute<X, Object, Object, Collection<Object>>(viewType, this, context, attributeIndex, dirtyStateIndex, embeddableMapping);
                        } else {
                            attribute = new MappingMethodMultiMapAttribute<X, Object, Object, Collection<Object>>(viewType, this, context, attributeIndex, dirtyStateIndex, embeddableMapping);
                        }
                    }
                } else {
                    context.addError("The mapping defined on method '" + viewType.getJavaType().getName() + "." + method.getName() + "' uses a an unknown collection type: " + declaredTypeClass);
                }
            } else {
                if (mapping instanceof MappingSubquery) {
                    attribute = new SubqueryMethodSingularAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex);
                } else if (correlated) {
                    attribute = new CorrelatedMethodSingularAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex, embeddableMapping);
                } else {
                    attribute = new MappingMethodSingularAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex, embeddableMapping);
                }
            }
        } else if (dirtyStateIndex != -1) {
            throw new IllegalStateException("Already constructed attribute with dirtyStateIndex " + ((AbstractMethodAttribute<?, ?>) attribute).getDirtyStateIndex() + " but now a different index " + dirtyStateIndex + " is requested!");
        }

        if (embeddableMapping == null) {
            this.attribute = attribute;
        } else {
            embeddableAttributeMap.put(embeddableMapping, attribute);
        }

        return (AbstractMethodAttribute<? super X, ?>) attribute;
    }
}
