/*
 * Copyright 2014 - 2016 Blazebit.
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
import javax.persistence.metamodel.Metamodel;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is a wrapper around the JPA {@link Metamodel} allows additionally efficient access by other attributes than a Class.
 *
 * @author Christian Beikov
 * @since 1.2
 */
public class EntityMetamodel implements Metamodel {

    private final Metamodel delegate;
    private final Map<String, EntityType<?>> entityNameMap;
    private final Map<Class<?>, ManagedType<?>> classMap;
    private final Map<Class<?>, ManagedType<?>> cteMap;
    private final Map<Class<?>, Map<String, Map.Entry<AttributePath, String[]>>> typeAttributeColumnNameMap;
    private final Map<Class<?>, Map<String, Map.Entry<AttributePath, String[]>>> typeAttributeColumnTypeMap;

    public EntityMetamodel(EntityManagerFactory emf, ExtendedQuerySupport extendedQuerySupport) {
        this.delegate = emf.getMetamodel();
        Set<ManagedType<?>> managedTypes = delegate.getManagedTypes();
        Map<String, EntityType<?>> nameToType = new HashMap<String, EntityType<?>>(managedTypes.size());
        Map<Class<?>, ManagedType<?>> classToType = new HashMap<Class<?>, ManagedType<?>>(managedTypes.size());
        Map<Class<?>, ManagedType<?>> cteToType = new HashMap<Class<?>, ManagedType<?>>(managedTypes.size());
        Map<Class<?>, Map<String, Map.Entry<AttributePath, String[]>>> typeAttributeColumnNames = new HashMap<Class<?>, Map<String, Map.Entry<AttributePath, String[]>>>(managedTypes.size());
        Map<Class<?>, Map<String, Map.Entry<AttributePath, String[]>>> typeAttributeColumnTypeNames = new HashMap<Class<?>, Map<String, Map.Entry<AttributePath, String[]>>>(managedTypes.size());
        EntityManager em = emf.createEntityManager();

        for (ManagedType<?> t : managedTypes) {
            if (t instanceof EntityType<?>) {
                EntityType<?> e = (EntityType<?>) t;
                nameToType.put(e.getName(), e);

                if (extendedQuerySupport != null && extendedQuerySupport.supportsAdvancedSql()) {
                    Set<Attribute<?, ?>> attributes = (Set<Attribute<?, ?>>) t.getAttributes();

                    Map<String, Map.Entry<AttributePath, String[]>> attributeMap = new HashMap<>(attributes.size());
                    typeAttributeColumnNames.put(t.getJavaType(), Collections.unmodifiableMap(attributeMap));

                    Map<String, Map.Entry<AttributePath, String[]>> attributeTypeMap = new HashMap<>(attributes.size());
                    typeAttributeColumnTypeNames.put(t.getJavaType(), Collections.unmodifiableMap(attributeTypeMap));

                    for (Attribute<?, ?> attribute : attributes) {
                        Class<?> fieldType = JpaUtils.resolveFieldClass(t.getJavaType(), attribute);
                        if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                            collectColumnNames(extendedQuerySupport, em, e, attributeMap, attribute.getName(), delegate.embeddable(fieldType));
                        }

                        AttributePath path = new AttributePath(Arrays.<Attribute<?, ?>>asList(attribute), fieldType);

                        // Collect column names
                        String[] columnNames = extendedQuerySupport.getColumnNames(em, e, attribute.getName());
                        attributeMap.put(attribute.getName(), new AbstractMap.SimpleEntry<AttributePath, String[]>(path, columnNames));

                        // Collect column types
                        String[] columnTypes = extendedQuerySupport.getColumnTypes(em, e, attribute.getName());
                        attributeTypeMap.put(attribute.getName(), new AbstractMap.SimpleEntry<AttributePath, String[]>(path, columnTypes));
                    }
                }
            }

            classToType.put(t.getJavaType(), t);

            if (AnnotationUtils.findAnnotation(t.getJavaType(), CTE.class) != null) {
                cteToType.put(t.getJavaType(), t);
            }
        }

        this.entityNameMap = Collections.unmodifiableMap(nameToType);
        this.classMap = Collections.unmodifiableMap(classToType);
        this.cteMap = Collections.unmodifiableMap(cteToType);
        this.typeAttributeColumnNameMap = Collections.unmodifiableMap(typeAttributeColumnNames);
        this.typeAttributeColumnTypeMap = Collections.unmodifiableMap(typeAttributeColumnTypeNames);
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
    public <X> ManagedType<X> managedType(Class<X> cls) {
        return delegate.managedType(cls);
    }

    public ManagedType<?> managedType(String name) {
        ManagedType<?> t = entityNameMap.get(name);
        if (t == null) {
            throw new IllegalStateException("Managed type with name '" + name + "' does not exist!");
        }

        return t;
    }

    @SuppressWarnings({ "unchecked" })
    public <X> ManagedType<X> getManagedType(Class<X> cls) {
        return (ManagedType<X>) classMap.get(cls);
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
