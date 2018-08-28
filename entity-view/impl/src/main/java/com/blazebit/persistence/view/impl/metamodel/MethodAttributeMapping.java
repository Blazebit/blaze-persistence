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

import com.blazebit.persistence.spi.AttributePath;
import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedMethodCollectionAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedMethodListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedMethodSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedMethodSetAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingMethodCollectionAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingMethodListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingMethodMapAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingMethodSetAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingMethodSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.SubqueryMethodSingularAttribute;
import com.blazebit.persistence.view.spi.EntityViewMapping;
import com.blazebit.persistence.view.spi.EntityViewMethodAttributeMapping;
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Type;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MethodAttributeMapping extends AttributeMapping implements EntityViewMethodAttributeMapping {

    private final String attributeName;
    private final Method method;

    // Updatable configs
    private Boolean isUpdatable;
    private Boolean isOrphanRemoval;
    private Boolean isOptimisticLockProtected;
    private String mappedBy;
    private boolean mappedByResolved;
    private InverseRemoveStrategy inverseRemoveStrategy;
    private Set<CascadeType> cascadeTypes = Collections.singleton(CascadeType.AUTO);
    private Set<Class<?>> cascadeSubtypeClasses;
    private Set<Class<?>> cascadePersistSubtypeClasses;
    private Set<Class<?>> cascadeUpdateSubtypeClasses;

    private Map<ViewMapping, Boolean> cascadeSubtypeMappings;
    private Map<ViewMapping, Boolean> cascadePersistSubtypeMappings;
    private Map<ViewMapping, Boolean> cascadeUpdateSubtypeMappings;
    private Set<ManagedViewTypeImplementor<?>> cascadeSubtypes;
    private Set<ManagedViewTypeImplementor<?>> cascadePersistSubtypes;
    private Set<ManagedViewTypeImplementor<?>> cascadeUpdateSubtypes;

    // TODO: attribute filter config

    public MethodAttributeMapping(ViewMapping viewMapping, Annotation mapping, MetamodelBootContext context, String attributeName, Method method, boolean isCollection, Class<?> declaredTypeClass, Class<?> declaredKeyTypeClass, Class declaredElementTypeClass,
                                  java.lang.reflect.Type type, java.lang.reflect.Type keyType, java.lang.reflect.Type elementType, Map<Class<?>, String> inheritanceSubtypeClassMappings, Map<Class<?>, String> keyInheritanceSubtypeClassMappings, Map<Class<?>, String> elementInheritanceSubtypeClassMappings) {
        super(viewMapping, mapping, context, isCollection, declaredTypeClass, declaredKeyTypeClass, declaredElementTypeClass, type, keyType, elementType, inheritanceSubtypeClassMappings, keyInheritanceSubtypeClassMappings, elementInheritanceSubtypeClassMappings);
        this.attributeName = attributeName;
        this.method = method;
    }

    @Override
    public EntityViewMapping getDeclaringView() {
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
        this.mappedByResolved = true;
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

    public Set<ManagedViewTypeImplementor<?>> getCascadeSubtypes(MetamodelBuildingContext context) {
        if (cascadeSubtypes != null) {
            return cascadeSubtypes;
        }
        return cascadeSubtypes = initializeCascadeSubtypes(cascadeSubtypeMappings, context);
    }

    public Set<ManagedViewTypeImplementor<?>> getCascadePersistSubtypes(MetamodelBuildingContext context) {
        if (cascadePersistSubtypes != null) {
            return cascadePersistSubtypes;
        }
        return cascadePersistSubtypes = initializeCascadeSubtypes(cascadePersistSubtypeMappings, context);
    }

    public Set<ManagedViewTypeImplementor<?>> getCascadeUpdateSubtypes(MetamodelBuildingContext context) {
        if (cascadeUpdateSubtypes != null) {
            return cascadeUpdateSubtypes;
        }
        return cascadeUpdateSubtypes = initializeCascadeSubtypes(cascadeUpdateSubtypeMappings, context);
    }

    private Set<ManagedViewTypeImplementor<?>> initializeCascadeSubtypes(Map<ViewMapping, Boolean> subtypeMappings, MetamodelBuildingContext context) {
        if (subtypeMappings == null || subtypeMappings.isEmpty()) {
            return Collections.emptySet();
        }
        Set<ManagedViewTypeImplementor<?>> subtypes = new HashSet<>(subtypeMappings.size());
        for (ViewMapping mapping : subtypeMappings.keySet()) {
            subtypes.add(mapping.getManagedViewType(context));
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

    @Override
    public void initializeViewMappings(MetamodelBuildingContext context) {
        super.initializeViewMappings(context);

        if (isEmpty(cascadeSubtypeClasses) && isEmpty(cascadePersistSubtypeClasses) && isEmpty(cascadeUpdateSubtypeClasses)) {
            // If no classes are given, we try to find all subtype classes
            Method setter = ReflectionUtils.getSetter(getDeclaringView().getEntityViewClass(), getName());
            boolean hasSetter = setter != null && (setter.getModifiers() & Modifier.ABSTRACT) != 0;
            boolean isCollection = false;

            ViewMapping attributeViewMapping;
            if (elementViewMapping == null) {
                attributeViewMapping = typeMapping;
            } else {
                attributeViewMapping = elementViewMapping;
                isCollection = true;
            }
            // Also see AbstractMethodPluralAttribute#determineUpdatable() for the same logic
            if (attributeViewMapping != null && (isUpdatable == Boolean.TRUE || getDeclaringView().isUpdatable())) {
                if (hasSetter || isCollection && (cascadeTypes.contains(CascadeType.PERSIST) || attributeViewMapping.isCreatable())) {
                    // But only if the attribute is explicitly or implicitly updatable
                    this.cascadeSubtypeMappings = initializeDependentCascadeSubtypeMappingsAuto(context, attributeViewMapping.getEntityViewClass());
                } else {
                    this.cascadeSubtypeMappings = Collections.emptyMap();
                }
            } else {
                this.cascadeSubtypeMappings = Collections.emptyMap();
            }
            this.cascadePersistSubtypeMappings = Collections.emptyMap();
            this.cascadeUpdateSubtypeMappings = Collections.emptyMap();
        } else if (isUpdatable == Boolean.TRUE) {
            this.cascadeSubtypeMappings = initializeDependentCascadeSubtypeMappings(context, cascadeSubtypeClasses);
            this.cascadePersistSubtypeMappings = initializeDependentCascadeSubtypeMappings(context, cascadePersistSubtypeClasses);
            this.cascadeUpdateSubtypeMappings = initializeDependentCascadeSubtypeMappings(context, cascadeUpdateSubtypeClasses);
        }
    }

    private static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    @Override
    public boolean validateDependencies(MetamodelBuildingContext context, Set<Class<?>> dependencies, boolean reportError) {
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

    public String determineMappedBy(ManagedType<?> managedType, String mapping, MetamodelBuildingContext context) {
        if (mappedByResolved) {
            return mappedBy;
        }

        mappedByResolved = true;

        if (mapping == null || mapping.isEmpty()) {
            return null;
        }
        if (!(managedType instanceof EntityType<?>)) {
            // Can't determine the inverse mapped by attribute of a non-entity
            return null;
        }

        // If we find a non-simple path, we don't even try to find a mapped by mapping
        for (int i = 0; i < mapping.length(); i++) {
            final char c = mapping.charAt(i);
            if (!Character.isJavaIdentifierPart(c) && c != '.') {
                return null;
            }
        }

        try {
            AttributePath basicAttributePath = context.getJpaProvider().getJpaMetamodelAccessor().getAttributePath(context.getEntityMetamodel(), managedType, mapping);
            List<Attribute<?, ?>> attributes = basicAttributePath.getAttributes();
            for (int i = 0; i < attributes.size() - 1; i++) {
                if (attributes.get(i).getDeclaringType().getPersistenceType() != Type.PersistenceType.EMBEDDABLE) {
                    // If the mapping goes over a non-embeddable, we can't determine a mapped by attribute name
                    return null;
                }
            }
            return mappedBy = context.getJpaProvider().getMappedBy((EntityType<?>) managedType, mapping);
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
            Map<String, String> writableMappedByMappings = context.getJpaProvider().getWritableMappedByMappings(entityType, elementType, mappedBy);
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
            if (mapping.validateDependencies(context, dependencies, this, null, reportError)) {
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

    private Map<ViewMapping, Boolean> initializeDependentCascadeSubtypeMappings(MetamodelBuildingContext context, Set<Class<?>> subtypes) {
        if (subtypes.size() == 0) {
            return Collections.emptyMap();
        }

        Map<ViewMapping, Boolean> subtypeMappings = new HashMap<>(subtypes.size());
        for (Class<?> type : subtypes) {
            ViewMapping subtypeMapping = context.getViewMapping(type);
            if (subtypeMapping == null) {
                unknownSubviewType(type);
            } else {
                subtypeMapping.initializeViewMappings(context, null);
                subtypeMappings.put(subtypeMapping, Boolean.TRUE);
            }
        }

        return subtypeMappings;
    }

    private Map<ViewMapping, Boolean> initializeDependentCascadeSubtypeMappingsAuto(final MetamodelBuildingContext context, final Class<?> clazz) {
        Set<Class<?>> subtypes = context.findSubtypes(clazz);
        if (subtypes.size() == 0) {
            return Collections.emptyMap();
        }

        final Map<ViewMapping, Boolean> subtypeMappings = new HashMap<>(subtypes.size());
        for (Class<?> type : subtypes) {
            final ViewMapping subtypeMapping = context.getViewMapping(type);
            if (subtypeMapping == null) {
                unknownSubviewType(type);
            } else {
                // We can't initialize a potential subtype mapping here immediately, but have to wait until the current view mapping is fully initialized
                // This avoids access to partly initialized view mappings
                viewMapping.onInitializeViewMappingsFinished(new Runnable() {
                    @Override
                    public void run() {
                        subtypeMapping.onInitializeViewMappingsFinished(new Runnable() {
                            @Override
                            public void run() {
                                Set<Class<?>> dependencies = new HashSet<>();
                                dependencies.add(getDeclaringView().getEntityViewClass());
                                dependencies.add(clazz);
                                if (!subtypeMapping.validateDependencies(context, dependencies, MethodAttributeMapping.this, clazz, false)) {
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

    public MethodAttributeMapping handleReplacement(AttributeMapping original) {
        if (original == null) {
            return this;
        }
        if (!(original instanceof MethodAttributeMapping)) {
            throw new IllegalStateException("Tried to replace attribute [" + original + "] with method attribute: " + this);
        }

        MethodAttributeMapping originalAttribute = (MethodAttributeMapping) original;
        // If the mapping is the same, just let it through
        if (mapping.equals(originalAttribute.getMapping())) {
            return originalAttribute;
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

        context.addError("Conflicting attribute mapping for attribute '" + attributeName + "' at the methods [" + methodReference(method) + ", " + methodReference(originalAttribute.getMethod()) + "] for managed view type '" + viewMapping.getEntityViewClass().getName() + "'");
        return originalAttribute;
    }

    private static String methodReference(Method method) {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    // If you change something here don't forget to also update ParameterAttributeMapping#getParameterAttribute
    @SuppressWarnings("unchecked")
    public <X> AbstractMethodAttribute<? super X, ?> getMethodAttribute(ManagedViewTypeImplementor<X> viewType, int attributeIndex, int dirtyStateIndex, MetamodelBuildingContext context) {
        if (attribute == null) {
            if (mapping instanceof MappingParameter) {
                mappedByResolved = true;
                attribute = new MappingMethodSingularAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex);
                return (AbstractMethodAttribute<? super X, ?>) attribute;
            }

            boolean correlated = mapping instanceof MappingCorrelated || mapping instanceof MappingCorrelatedSimple;

            if (isCollection) {
                if (Collection.class == declaredTypeClass) {
                    if (correlated) {
                        attribute = new CorrelatedMethodCollectionAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex);
                    } else {
                        attribute = new MappingMethodCollectionAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex);
                    }
                } else if (List.class == declaredTypeClass) {
                    if (correlated) {
                        attribute = new CorrelatedMethodListAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex);
                    } else {
                        attribute = new MappingMethodListAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex);
                    }
                } else if (Set.class == declaredTypeClass || SortedSet.class == declaredTypeClass || NavigableSet.class == declaredTypeClass) {
                    if (correlated) {
                        attribute = new CorrelatedMethodSetAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex);
                    } else {
                        attribute = new MappingMethodSetAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex);
                    }
                } else if (Map.class == declaredTypeClass || SortedMap.class == declaredTypeClass || NavigableMap.class == declaredTypeClass) {
                    if (correlated) {
                        context.addError("The mapping defined on method '" + viewType.getJavaType().getName() + "." + method.getName() + "' uses a Map type with a correlated mapping which is unsupported!");
                        attribute = null;
                    } else {
                        attribute = new MappingMethodMapAttribute<X, Object, Object>(viewType, this, context, attributeIndex, dirtyStateIndex);
                    }
                } else {
                    context.addError("The mapping defined on method '" + viewType.getJavaType().getName() + "." + method.getName() + "' uses a an unknown collection type: " + declaredTypeClass);
                }
            } else {
                if (mapping instanceof MappingSubquery) {
                    attribute = new SubqueryMethodSingularAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex);
                } else if (correlated) {
                    attribute = new CorrelatedMethodSingularAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex);
                } else {
                    attribute = new MappingMethodSingularAttribute<X, Object>(viewType, this, context, attributeIndex, dirtyStateIndex);
                }
            }
        } else if (dirtyStateIndex != -1) {
            throw new IllegalStateException("Already constructed attribute with dirtyStateIndex " + ((AbstractMethodAttribute<?, ?>) attribute).getDirtyStateIndex() + " but now a different index " + dirtyStateIndex + " is requested!");
        }

        return (AbstractMethodAttribute<? super X, ?>) attribute;
    }
}
