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

import com.blazebit.persistence.view.BatchFetch;
import com.blazebit.persistence.view.CollectionMapping;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.metamodel.Type;

import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AttributeMapping {

    protected final Class<?> entityViewClass;
    protected final ManagedType<?> managedType;
    protected final Annotation mapping;
    protected final MetamodelBuildingContext context;
    protected Boolean isId;
    protected Type<?> type;
    protected Type<?> keyType;
    protected Type<?> elementType;
    protected ViewMapping typeMapping;
    protected ViewMapping keyViewMapping;
    protected ViewMapping elementViewMapping;
    protected InheritanceViewMapping inheritanceSubtypeMappings;
    protected InheritanceViewMapping keyInheritanceSubtypeMappings;
    protected InheritanceViewMapping elementInheritanceSubtypeMappings;
    protected Map<ManagedViewTypeImpl<?>, String> inheritanceSubtypes;
    protected Map<ManagedViewTypeImpl<?>, String> keyInheritanceSubtypes;
    protected Map<ManagedViewTypeImpl<?>, String> elementInheritanceSubtypes;
    protected AbstractAttribute<?, ?> attribute;

    public AttributeMapping(Class<?> entityViewClass, ManagedType<?> managedType, Annotation mapping, MetamodelBuildingContext context) {
        this.entityViewClass = entityViewClass;
        this.managedType = managedType;
        this.mapping = mapping;
        this.context = context;
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

    public boolean isId() {
        if (isId == null) {
            if (mapping instanceof IdMapping) {
                if (!(managedType instanceof IdentifiableType<?>)) {
                    context.addError("Invalid id attribute mapping for embeddable entity type '" + managedType.getJavaType().getName() + "' at " + getErrorLocation() + " for managed view type '" + entityViewClass.getName() + "'!");
                }
                return isId = Boolean.TRUE;
            }

            return isId = Boolean.FALSE;
        }

        return isId;
    }

    public abstract String getErrorLocation();

    public boolean isSorted() {
        return MetamodelUtils.isSorted(getJavaType());
    }

    public boolean isIndexed() {
        String mappingExpression;
        if (mapping instanceof IdMapping) {
            mappingExpression = ((IdMapping) mapping).value();
        } else if (mapping instanceof Mapping) {
            mappingExpression = ((Mapping) mapping).value();
        } else {
            // Correlated mappings, parameter mappings and subqueries are never indexed
            return false;
        }
        return MetamodelUtils.isIndexedList(context.getEntityMetamodel(), context.getExpressionFactory(), managedType.getJavaType(), AbstractAttribute.stripThisFromMapping(mappingExpression));
    }

    public abstract CollectionMapping getCollectionMapping();

    public abstract BatchFetch getBatchFetch();

    public Class<?> getJavaType() {
        Type<?> t = getType();
        if (t == null) {
            return null;
        }
        return t.getJavaType();
    }

    public Type<?> getType() {
        if (type != null) {
            return type;
        }
        if (typeMapping == null) {
            context.addError("The type is not resolvable for the " + getErrorLocation() + "!");
            return null;
        }
        return type = typeMapping.getManagedViewType();
    }

    public Type<?> getKeyType() {
        if (keyType != null) {
            return keyType;
        }
        if (keyViewMapping == null) {
            context.addError("The key type is not resolvable for the " + getErrorLocation() + "!");
            return null;
        }
        return keyType = keyViewMapping.getManagedViewType();
    }

    public Type<?> getElementType() {
        if (elementType != null) {
            return elementType;
        }
        if (elementViewMapping == null) {
            context.addError("The element type is not resolvable for the " + getErrorLocation() + "!");
            return null;
        }
        return elementType = elementViewMapping.getManagedViewType();
    }

    public Map<ManagedViewTypeImpl<?>, String> getInheritanceSubtypes() {
        if (inheritanceSubtypes != null) {
            return inheritanceSubtypes;
        }
        return inheritanceSubtypes = initializeInheritanceSubtypes(inheritanceSubtypeMappings, typeMapping);
    }

    public Map<ManagedViewTypeImpl<?>, String> getKeyInheritanceSubtypes() {
        if (keyInheritanceSubtypes != null) {
            return keyInheritanceSubtypes;
        }
        return keyInheritanceSubtypes = initializeInheritanceSubtypes(keyInheritanceSubtypeMappings, keyViewMapping);
    }

    public Map<ManagedViewTypeImpl<?>, String> getElementInheritanceSubtypes() {
        if (elementInheritanceSubtypes != null) {
            return elementInheritanceSubtypes;
        }
        return elementInheritanceSubtypes = initializeInheritanceSubtypes(elementInheritanceSubtypeMappings, elementViewMapping);
    }

    @SuppressWarnings("unchecked")
    private Map<ManagedViewTypeImpl<?>, String> initializeInheritanceSubtypes(InheritanceViewMapping inheritanceSubtypeMappings, ViewMapping viewMapping) {
        if (inheritanceSubtypeMappings == null || inheritanceSubtypeMappings.getInheritanceSubtypeMappings().isEmpty()) {
            return Collections.emptyMap();
        }
        Map<ManagedViewTypeImpl<?>, String> map = new LinkedHashMap<>(inheritanceSubtypeMappings.getInheritanceSubtypeMappings().size());
        for (Map.Entry<ViewMapping, String> mappingEntry : inheritanceSubtypeMappings.getInheritanceSubtypeMappings().entrySet()) {
            String mapping = mappingEntry.getValue();
            if (mapping == null) {
                mapping = mappingEntry.getKey().getInheritanceMapping();
                // An empty inheritance mapping signals that a subtype should actually be considered. If it was null it wouldn't be considered
                if (mapping == null) {
                    mapping = "";
                }
            }
            map.put(mappingEntry.getKey().getManagedViewType(), mapping);
        }
        if (map.equals(viewMapping.getManagedViewType().getInheritanceSubtypeConfiguration())) {
            return (Map<ManagedViewTypeImpl<?>, String>) (Map<?, ?>) viewMapping.getManagedViewType().getInheritanceSubtypeConfiguration();
        } else {
            return Collections.unmodifiableMap(map);
        }
    }

    protected abstract Class<?> resolveType();

    protected abstract Class<?> resolveKeyType();

    protected abstract Class<?> resolveElementType();

    protected abstract Map<Class<?>, String> resolveInheritanceSubtypeMappings();

    protected abstract Map<Class<?>, String> resolveKeyInheritanceSubtypeMappings();

    protected abstract Map<Class<?>, String> resolveElementInheritanceSubtypeMappings();

    public void initializeViewMappings(Class<?> entityViewRootClass, Map<Class<?>, ViewMapping> viewMappings, Set<Class<?>> dependencies) {
        Class<?> type = resolveType();
        Class<?> keyType = resolveKeyType();
        Class<?> elementType = resolveElementType();

        if (context.isEntityView(type)) {
            typeMapping = initializeDependentMapping(entityViewRootClass, type, viewMappings, dependencies);
            inheritanceSubtypeMappings = initializedInheritanceViewMappings(typeMapping, resolveInheritanceSubtypeMappings(), entityViewRootClass, viewMappings, dependencies);
        } else {
            this.type = context.getBasicType(type);
        }
        if (context.isEntityView(keyType)) {
            keyViewMapping = initializeDependentMapping(entityViewRootClass, keyType, viewMappings, dependencies);
            keyInheritanceSubtypeMappings = initializedInheritanceViewMappings(keyViewMapping, resolveKeyInheritanceSubtypeMappings(), entityViewRootClass, viewMappings, dependencies);
        } else {
            this.keyType = context.getBasicType(keyType);
        }
        if (context.isEntityView(elementType)) {
            elementViewMapping = initializeDependentMapping(entityViewRootClass, elementType, viewMappings, dependencies);
            elementInheritanceSubtypeMappings = initializedInheritanceViewMappings(elementViewMapping, resolveElementInheritanceSubtypeMappings(), entityViewRootClass, viewMappings, dependencies);
        } else {
            this.elementType = context.getBasicType(elementType);
        }
    }

    private InheritanceViewMapping initializedInheritanceViewMappings(ViewMapping attributeMapping, Map<Class<?>, String> inheritanceMapping, Class<?> entityViewRootClass, Map<Class<?>, ViewMapping> viewMappings, Set<Class<?>> dependencies) {
        InheritanceViewMapping inheritanceViewMapping;
        Map<ViewMapping, String> subtypeMappings = new HashMap<>();
        if (attributeMapping != null) {
            if (inheritanceMapping == null) {
                inheritanceViewMapping = attributeMapping.getDefaultInheritanceViewMapping();
            } else {
                subtypeMappings = new HashMap<>(inheritanceMapping.size() + 1);

                for (Map.Entry<Class<?>, String> mappingEntry : inheritanceMapping.entrySet()) {
                    subtypeMappings.put(initializeDependentMapping(entityViewRootClass, mappingEntry.getKey(), viewMappings, dependencies), mappingEntry.getValue());
                }

                inheritanceViewMapping = new InheritanceViewMapping(subtypeMappings);
                attributeMapping.getInheritanceViewMappings().add(inheritanceViewMapping);
                return inheritanceViewMapping;
            }
        } else {
            inheritanceViewMapping = new InheritanceViewMapping(subtypeMappings);
        }

        return inheritanceViewMapping;
    }

    private ViewMapping initializeDependentMapping(Class<?> entityViewRootClass, Class<?> subviewClass, Map<Class<?>, ViewMapping> viewMappings, Set<Class<?>> dependencies) {
        if (dependencies.contains(subviewClass)) {
            circularDependencyError(dependencies);
            return null;
        }

        dependencies.add(subviewClass);
        // This will initialize all subviews and populate the viewMappings map accordingly
        ViewMapping mapping = ViewMapping.initializeViewMappings(entityViewRootClass, subviewClass, context, viewMappings, dependencies, this);
        dependencies.remove(subviewClass);

        return mapping;
    }

    public void circularDependencyError(Set<Class<?>> dependencies) {
        context.addError("A circular dependency is introduced at the " + getErrorLocation() + " in the following dependency set: " + Arrays.deepToString(dependencies.toArray()));
    }
}
