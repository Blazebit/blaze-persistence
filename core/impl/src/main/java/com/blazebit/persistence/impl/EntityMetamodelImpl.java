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

package com.blazebit.persistence.impl;

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.persistence.CTE;
import com.blazebit.persistence.spi.ExtendedQuerySupport;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.Type;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is a wrapper around the JPA {@link Metamodel} allows additionally efficient access by other attributes than a Class.
 *
 * @author Christian Beikov
 * @since 1.2
 */
public class EntityMetamodelImpl implements EntityMetamodel {

    private final Metamodel delegate;
    private final Map<String, EntityType<?>> entityNameMap;
    private final Map<String, Class<?>> entityTypes;
    private final Map<String, Class<Enum<?>>> enumTypes;
    private final Map<Class<?>, ManagedType<?>> classMap;
    private final Map<Class<?>, ManagedType<?>> cteMap;
    private final Map<Class<?>, Map<String, Map.Entry<AttributePath, String[]>>> typeAttributeColumnNameMap;
    private final Map<Class<?>, Map<String, Map.Entry<AttributePath, String[]>>> typeAttributeColumnTypeMap;

    public EntityMetamodelImpl(EntityManagerFactory emf, ExtendedQuerySupport extendedQuerySupport) {
        this.delegate = emf.getMetamodel();
        Set<ManagedType<?>> managedTypes = delegate.getManagedTypes();
        Map<String, EntityType<?>> nameToType = new HashMap<>(managedTypes.size());
        Map<String, Class<?>> entityTypes = new HashMap<>(managedTypes.size());
        Map<String, Class<Enum<?>>> enumTypes = new HashMap<>(managedTypes.size());
        Map<Class<?>, ManagedType<?>> classToType = new HashMap<>(managedTypes.size());
        Map<Class<?>, ManagedType<?>> cteToType = new HashMap<>(managedTypes.size());
        Map<Class<?>, Map<String, Map.Entry<AttributePath, String[]>>> typeAttributeColumnNames = new HashMap<>(managedTypes.size());
        Map<Class<?>, Map<String, Map.Entry<AttributePath, String[]>>> typeAttributeColumnTypeNames = new HashMap<>(managedTypes.size());
        EntityManager em = emf.createEntityManager();

        Set<Class<?>> seenTypesForEnumResolving = new HashSet<>();

        try {
            for (ManagedType<?> t : managedTypes) {
                if (t instanceof EntityType<?>) {
                    EntityType<?> e = (EntityType<?>) t;
                    nameToType.put(e.getName(), e);
                    entityTypes.put(e.getName(), e.getJavaType());
                    entityTypes.put(e.getJavaType().getName(), e.getJavaType());

                    if (extendedQuerySupport != null && extendedQuerySupport.supportsAdvancedSql()) {
                        Set<Attribute<?, ?>> attributes = (Set<Attribute<?, ?>>) t.getAttributes();

                        Map<String, Map.Entry<AttributePath, String[]>> attributeMap = new HashMap<>(attributes.size());
                        typeAttributeColumnNames.put(t.getJavaType(), Collections.unmodifiableMap(attributeMap));

                        Map<String, Map.Entry<AttributePath, String[]>> attributeTypeMap = new HashMap<>(attributes.size());
                        typeAttributeColumnTypeNames.put(t.getJavaType(), Collections.unmodifiableMap(attributeTypeMap));

                        seenTypesForEnumResolving.add(t.getJavaType());

                        for (Attribute<?, ?> attribute : attributes) {
                            Class<?> fieldType = JpaUtils.resolveFieldClass(t.getJavaType(), attribute);
                            if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                                collectColumnNames(extendedQuerySupport, em, e, attributeMap, attribute.getName(), delegate.embeddable(fieldType));
                            }

                            AttributePath path = new AttributePath(Arrays.<Attribute<?, ?>>asList(attribute), fieldType);

                            // Collect column names
                            String[] columnNames = extendedQuerySupport.getColumnNames(em, e, attribute.getName());
                            attributeMap.put(attribute.getName(), new AbstractMap.SimpleEntry<>(path, columnNames));

                            // Collect column types
                            String[] columnTypes = extendedQuerySupport.getColumnTypes(em, e, attribute.getName());
                            attributeTypeMap.put(attribute.getName(), new AbstractMap.SimpleEntry<>(path, columnTypes));

                            discoverEnumTypes(seenTypesForEnumResolving, enumTypes, e.getJavaType(), attribute);
                        }
                    } else {
                        discoverEnumTypes(seenTypesForEnumResolving, enumTypes, t);
                    }
                } else {
                    discoverEnumTypes(seenTypesForEnumResolving, enumTypes, t);
                }

                classToType.put(t.getJavaType(), t);

                if (AnnotationUtils.findAnnotation(t.getJavaType(), CTE.class) != null) {
                    cteToType.put(t.getJavaType(), t);
                }
            }
        } finally {
            em.close();
        }

        this.entityNameMap = Collections.unmodifiableMap(nameToType);
        this.entityTypes = Collections.unmodifiableMap(entityTypes);
        this.enumTypes = Collections.unmodifiableMap(enumTypes);
        this.classMap = Collections.unmodifiableMap(classToType);
        this.cteMap = Collections.unmodifiableMap(cteToType);
        this.typeAttributeColumnNameMap = Collections.unmodifiableMap(typeAttributeColumnNames);
        this.typeAttributeColumnTypeMap = Collections.unmodifiableMap(typeAttributeColumnTypeNames);
    }

    private void discoverEnumTypes(Set<Class<?>> seenTypesForEnumResolving, Map<String, Class<Enum<?>>> enumTypes, ManagedType<?> t) {
        if (!seenTypesForEnumResolving.add(t.getJavaType())) {
            return;
        }
        for (Attribute<?, ?> attribute : (Set<Attribute<?, ?>>) t.getAttributes()) {
            discoverEnumTypes(seenTypesForEnumResolving, enumTypes, t.getJavaType(), attribute);
        }
    }

    private void discoverEnumTypes(Set<Class<?>> seenTypesForEnumResolving, Map<String, Class<Enum<?>>> enumTypes, Type<?> type) {
        if (type.getPersistenceType() == Type.PersistenceType.BASIC) {
            Class<?> elementType = type.getJavaType();
            if (elementType.isEnum()) {
                enumTypes.put(elementType.getName(), (Class<Enum<?>>) elementType);
            }
        } else {
            discoverEnumTypes(seenTypesForEnumResolving, enumTypes, (ManagedType<?>) type);
        }
    }

    private void discoverEnumTypes(Set<Class<?>> seenTypesForEnumResolving, Map<String, Class<Enum<?>>> enumTypes, Class<?> baseType, Attribute<?, ?> attribute) {
        Class<?> fieldType = JpaUtils.resolveFieldClass(baseType, attribute);
        if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC) {
            if (fieldType.isEnum()) {
                enumTypes.put(fieldType.getName(), (Class<Enum<?>>) fieldType);
            }
        } else if (attribute.isCollection()) {
            PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
            if (pluralAttribute.getCollectionType() == PluralAttribute.CollectionType.MAP) {
                MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) pluralAttribute;
                discoverEnumTypes(seenTypesForEnumResolving, enumTypes, mapAttribute.getKeyType());
            }

            discoverEnumTypes(seenTypesForEnumResolving, enumTypes, pluralAttribute.getElementType());
        } else if (!seenTypesForEnumResolving.contains(fieldType)) {
            discoverEnumTypes(seenTypesForEnumResolving, enumTypes, delegate.managedType(fieldType));
        }
    }

    private void collectColumnNames(ExtendedQuerySupport extendedQuerySupport, EntityManager em, EntityType<?> e, Map<String, Map.Entry<AttributePath, String[]>> attributeMap, String parent, EmbeddableType<?> type) {
        Set<Attribute<?, ?>> attributes = (Set<Attribute<?, ?>>) type.getAttributes();

        for (Attribute<?, ?> attribute : attributes) {
            Class<?> fieldType = JpaUtils.resolveFieldClass(type.getJavaType(), attribute);
            if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                collectColumnNames(extendedQuerySupport, em, e, attributeMap, parent + "." + attribute.getName(), delegate.embeddable(fieldType));
            }

            String attributeName = parent + "." + attribute.getName();
            String[] columnNames = extendedQuerySupport.getColumnNames(em, e, attributeName);
            AttributePath path = new AttributePath(Arrays.<Attribute<?, ?>>asList(attribute), fieldType);
            attributeMap.put(attributeName, new AbstractMap.SimpleEntry<AttributePath, String[]>(path, columnNames));
        }
    }

    public Map<String, Map.Entry<AttributePath, String[]>> getAttributeColumnNameMapping(Class<?> cls) {
        return typeAttributeColumnNameMap.get(cls);
    }

    public Map<String, Map.Entry<AttributePath, String[]>> getAttributeColumnTypeMapping(Class<?> cls) {
        return typeAttributeColumnTypeMap.get(cls);
    }

    @Override
    public <X> EntityType<X> entity(Class<X> cls) {
        return delegate.entity(cls);
    }

    @Override
    public EntityType<?> getEntity(String name) {
        return entityNameMap.get(name);
    }

    @Override
    public ManagedType<?> getManagedType(String name) {
        return entityNameMap.get(name);
    }

    public Map<String, Class<?>> getEntityTypes() {
        return entityTypes;
    }

    public Map<String, Class<Enum<?>>> getEnumTypes() {
        return enumTypes;
    }

    @Override
    public <X> ManagedType<X> managedType(Class<X> cls) {
        return delegate.managedType(cls);
    }

    @Override
    public ManagedType<?> managedType(String name) {
        ManagedType<?> t = entityNameMap.get(name);
        if (t == null) {
            throw new IllegalStateException("Managed type with name '" + name + "' does not exist!");
        }

        return t;
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public <X> ManagedType<X> getManagedType(Class<X> cls) {
        return (ManagedType<X>) classMap.get(cls);
    }

    @Override
    public <X> EntityType<X> getEntity(Class<X> cls) {
        ManagedType<?> type = classMap.get(cls);
        if (type == null || !(type instanceof EntityType<?>)) {
            return null;
        }

        return (EntityType<X>) type;
    }

    @SuppressWarnings({ "unchecked" })
    public <X> ManagedType<X> getCte(Class<X> cls) {
        return (ManagedType<X>) cteMap.get(cls);
    }

    @Override
    public <X> EmbeddableType<X> embeddable(Class<X> cls) {
        return delegate.embeddable(cls);
    }

    @Override
    public Set<ManagedType<?>> getManagedTypes() {
        return delegate.getManagedTypes();
    }

    @Override
    public Set<EntityType<?>> getEntities() {
        return delegate.getEntities();
    }

    @Override
    public Set<EmbeddableType<?>> getEmbeddables() {
        return delegate.getEmbeddables();
    }
}
