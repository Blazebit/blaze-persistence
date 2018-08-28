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

package com.blazebit.persistence.impl;

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.persistence.CTE;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spi.JoinTable;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpaProviderFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.BasicType;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This is a wrapper around the JPA {@link Metamodel} allows additionally efficient access by other attributes than a Class.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityMetamodelImpl implements EntityMetamodel {

    private final Metamodel delegate;
    private final JpaProvider jpaProvider;
    private final Map<String, EntityType<?>> entityNameMap;
    private final Map<String, Class<?>> entityTypes;
    private final Map<String, Class<Enum<?>>> enumTypes;
    private final Map<Class<?>, Type<?>> classMap;
    private final ConcurrentMap<Class<?>, Type<?>> basicTypeMap = new ConcurrentHashMap<>();
    private final Map<Class<?>, ManagedType<?>> cteMap;
    private final Map<Object, ExtendedManagedTypeImpl<?>> extendedManagedTypes;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public EntityMetamodelImpl(EntityManagerFactory emf, JpaProviderFactory jpaProviderFactory) {
        this.delegate = emf.getMetamodel();
        Set<ManagedType<?>> managedTypes = delegate.getManagedTypes();
        Set<EntityType<?>> originalEntityTypes = delegate.getEntities();
        Map<String, EntityType<?>> nameToType = new HashMap<>(managedTypes.size());
        Map<String, Class<?>> entityTypes = new HashMap<>(managedTypes.size());
        Map<String, Class<Enum<?>>> enumTypes = new HashMap<>(managedTypes.size());
        Map<Class<?>, Type<?>> classToType = new HashMap<>(managedTypes.size());
        Map<Class<?>, ManagedType<?>> cteToType = new HashMap<>(managedTypes.size());
        EntityManager em = emf.createEntityManager();

        try {
            this.jpaProvider = jpaProviderFactory.createJpaProvider(em);
        } finally {
            em.close();
        }

        Set<Class<?>> seenTypesForEnumResolving = new HashSet<>();
        Map<String, TemporaryExtendedManagedType> temporaryExtendedManagedTypes = new HashMap<>();

        for (EntityType<?> e : originalEntityTypes) {
            // Only discover entity types
            nameToType.put(e.getName(), e);
            entityTypes.put(e.getName(), e.getJavaType());
            if (e.getJavaType() != null) {
                classToType.put(e.getJavaType(), e);
                entityTypes.put(e.getJavaType().getName(), e.getJavaType());
                seenTypesForEnumResolving.add(e.getJavaType());

                if (AnnotationUtils.findAnnotation(e.getJavaType(), CTE.class) != null) {
                    cteToType.put(e.getJavaType(), e);
                }
            }

            Set<Attribute<?, ?>> attributes = (Set<Attribute<?, ?>>) (Set) e.getAttributes();
            Map<String, AttributeEntry<?, ?>> attributeMap = new HashMap<>(attributes.size());
            TemporaryExtendedManagedType extendedManagedType = new TemporaryExtendedManagedType(e, attributeMap);
            temporaryExtendedManagedTypes.put(e.getName(), extendedManagedType);

            for (Attribute<?, ?> attribute : attributes) {
                List<Attribute<?, ?>> parents = Collections.<Attribute<?, ?>>singletonList(attribute);
                Class<?> fieldType = JpaMetamodelUtils.resolveFieldClass(e.getJavaType(), attribute);
                if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                    EmbeddableType<?> embeddableType;
                    // Hibernate Envers reports java.util.Map as type for the embedded id of an audited entity which we have to handle specially
                    if (fieldType == Map.class) {
                        embeddableType = (EmbeddableType<?>) ((SingularAttribute<?, ?>) attribute).getType();
                    } else {
                        embeddableType = delegate.embeddable(fieldType);
                    }
                    collectColumnNames(e, attributeMap, attribute.getName(), parents, embeddableType, temporaryExtendedManagedTypes);
                } else if (isAssociation(attribute) && !attribute.isCollection() && !jpaProvider.isForeignJoinColumn(e, attribute.getName())) {
                    collectIdColumns(attribute.getName(), e, e, attributeMap, attribute, fieldType);
                }

                attributeMap.put(attribute.getName(), new AttributeEntry(jpaProvider, e, attribute, attribute.getName(), fieldType, parents));
                discoverEnumTypes(seenTypesForEnumResolving, enumTypes, e.getJavaType(), attribute);
            }
        }

        for (ManagedType<?> t : managedTypes) {
            // we already checked all entity types, so skip these
            if (!(t instanceof EntityType<?>)) {
                TemporaryExtendedManagedType extendedManagedType = temporaryExtendedManagedTypes.get(t.getJavaType().getName());
                if (extendedManagedType == null) {
                    Set<Attribute<?, ?>> attributes = (Set<Attribute<?, ?>>) (Set) t.getAttributes();
                    Map<String, AttributeEntry<?, ?>> attributeMap = new HashMap<>(attributes.size());
                    for (Attribute<?, ?> attribute : attributes) {
                        Class<?> attributeType = JpaMetamodelUtils.resolveFieldClass(t.getJavaType(), attribute);
                        attributeMap.put(attribute.getName(), new AttributeEntry(jpaProvider, t, attribute, attribute.getName(), attributeType, Collections.singletonList(attribute)));
                    }
                    extendedManagedType = new TemporaryExtendedManagedType(t, attributeMap);
                    temporaryExtendedManagedTypes.put(t.getJavaType().getName(), extendedManagedType);
                }
                if (t.getJavaType() != null) {
                    classToType.put(t.getJavaType(), t);
                }
            }
        }

        Set<Class<?>> cascadingDeleteCycleSet = new HashSet<>();
        for (EntityType<?> e : originalEntityTypes) {
            Class<?> targetClass = e.getJavaType();
            TemporaryExtendedManagedType targetManagedType = temporaryExtendedManagedTypes.get(e.getName());
            cascadingDeleteCycleSet.add(targetClass);
            for (Map.Entry<String, AttributeEntry<?, ?>> entry : targetManagedType.attributes.entrySet()) {
                AttributeEntry<?, ?> attribute = entry.getValue();
                detectCascadingDeleteCycles(targetManagedType, temporaryExtendedManagedTypes, cascadingDeleteCycleSet, targetClass, attribute, classToType);
                if (targetManagedType.done) {
                    if (targetManagedType.cascadingDeleteCycle) {
                        entry.setValue(attribute.withCascadingDeleteCycle());
                    }
                }
            }
            cascadingDeleteCycleSet.remove(targetClass);
            targetManagedType.done = true;
        }

        Map<Object, ExtendedManagedTypeImpl<?>> extendedManagedTypes = new HashMap<>(temporaryExtendedManagedTypes.size());
        for (Map.Entry<String, TemporaryExtendedManagedType> entry : temporaryExtendedManagedTypes.entrySet()) {
            TemporaryExtendedManagedType value = entry.getValue();
            ExtendedManagedTypeImpl<?> extendedManagedType = new ExtendedManagedTypeImpl(value.managedType, value.cascadingDeleteCycle, initAttributes(value.attributes));
            extendedManagedTypes.put(entry.getKey(), extendedManagedType);
            if (value.managedType.getJavaType() != null) {
                extendedManagedTypes.put(value.managedType.getJavaType(), extendedManagedType);
            }
        }

        this.entityNameMap = Collections.unmodifiableMap(nameToType);
        this.entityTypes = Collections.unmodifiableMap(entityTypes);
        this.enumTypes = Collections.unmodifiableMap(enumTypes);
        this.classMap = Collections.unmodifiableMap(classToType);
        this.cteMap = Collections.unmodifiableMap(cteToType);
        this.extendedManagedTypes = Collections.unmodifiableMap(extendedManagedTypes);
    }

    private Map<String, AttributeEntry<?,?>> initAttributes(Map<String, AttributeEntry<?,?>> attributes) {
        for (AttributeEntry<?, ?> attributeEntry : attributes.values()) {
            attributeEntry.initColumnEquivalentAttributes(attributes.values());
        }

        return Collections.unmodifiableMap(attributes);
    }

    private void collectIdColumns(String prefix, EntityType<?> ownerType, EntityType<?> currentType, Map<String, AttributeEntry<?, ?>> attributeMap, Attribute<?, ?> attribute, Class<?> fieldType) {
        List<String> identifierOrUniqueKeyEmbeddedPropertyNames = jpaProvider.getIdentifierOrUniqueKeyEmbeddedPropertyNames(currentType, attribute.getName());

        for (String name : identifierOrUniqueKeyEmbeddedPropertyNames) {
            EntityType<?> fieldEntityType = delegate.entity(fieldType);
            Attribute<?, ?> idAttribute = JpaMetamodelUtils.getAttribute(fieldEntityType, name);
            Class<?> idType = JpaMetamodelUtils.resolveFieldClass(fieldType, idAttribute);
            String idPath = prefix + "." + name;
            attributeMap.put(idPath, new AttributeEntry(jpaProvider, ownerType, idAttribute, idPath, idType, Arrays.asList(attribute, idAttribute)));

            if (isAssociation(idAttribute) && !idAttribute.isCollection() && !jpaProvider.isForeignJoinColumn(ownerType, idAttribute.getName())) {
                collectIdColumns(idPath, ownerType, fieldEntityType, attributeMap, idAttribute, idType);
            }
        }
    }

    private void detectCascadingDeleteCycles(TemporaryExtendedManagedType ownerManagedType, Map<String, TemporaryExtendedManagedType> extendedManagedTypes, Set<Class<?>> cascadingDeleteCycleSet, Class<?> ownerClass, AttributeEntry<?, ?> attributeEntry, Map<Class<?>, Type<?>> classToType) {
        Class<?> targetClass = attributeEntry.getElementClass();
        Type<?> type = classToType.get(targetClass);
        String targetClassName = null;
        if (type != null) {
            if (type instanceof EntityType<?>) {
                targetClassName = ((EntityType<?>) type).getName();
            } else if (type.getJavaType() != null) {
                targetClassName = type.getJavaType().getName();
            }
        }
        TemporaryExtendedManagedType targetManagedType = extendedManagedTypes.get(targetClassName);
        if (targetManagedType != null) {
            if (cascadingDeleteCycleSet.add(targetClass)) {
                if (targetManagedType.done) {
                    // Mark owner as done if target already found a cascading delete cycle
                    if (targetManagedType.cascadingDeleteCycle) {
                        ownerManagedType.done = true;
                        ownerManagedType.cascadingDeleteCycle = true;
                    }
                } else {
                    for (Map.Entry<String, AttributeEntry<?, ?>> entry : targetManagedType.attributes.entrySet()) {
                        AttributeEntry<?, ?> attribute = entry.getValue();
                        detectCascadingDeleteCycles(targetManagedType, extendedManagedTypes, cascadingDeleteCycleSet, targetClass, attribute, classToType);
                        if (targetManagedType.done) {
                            if (targetManagedType.cascadingDeleteCycle) {
                                entry.setValue(attribute.withCascadingDeleteCycle());
                            }
                        }
                    }
                    targetManagedType.done = true;
                }

                cascadingDeleteCycleSet.remove(targetClass);
            } else {
                // Found a cascading delete cycle
                ownerManagedType.done = true;
                ownerManagedType.cascadingDeleteCycle = true;
                targetManagedType.done = true;
                targetManagedType.cascadingDeleteCycle = true;
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void discoverEnumTypes(Set<Class<?>> seenTypesForEnumResolving, Map<String, Class<Enum<?>>> enumTypes, ManagedType<?> t) {
        if (!(t instanceof EntityType<?>)) {
            // Only discover entity types
            return;
        }
        if (!seenTypesForEnumResolving.add(t.getJavaType())) {
            return;
        }
        for (Attribute<?, ?> attribute : (Set<Attribute<?, ?>>) (Set) t.getAttributes()) {
            discoverEnumTypes(seenTypesForEnumResolving, enumTypes, t.getJavaType(), attribute);
        }
    }

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
    private void discoverEnumTypes(Set<Class<?>> seenTypesForEnumResolving, Map<String, Class<Enum<?>>> enumTypes, Class<?> baseType, Attribute<?, ?> attribute) {
        Class<?> fieldType = JpaMetamodelUtils.resolveFieldClass(baseType, attribute);
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void collectColumnNames(EntityType<?> e, Map<String, AttributeEntry<?, ?>> attributeMap, String parent, List<Attribute<?, ?>> parents, EmbeddableType<?> type, Map<String, TemporaryExtendedManagedType> temporaryExtendedManagedTypes) {
        Set<Attribute<?, ?>> attributes = (Set<Attribute<?, ?>>) (Set) type.getAttributes();
        TemporaryExtendedManagedType extendedManagedType = temporaryExtendedManagedTypes.get(type.getJavaType().getName());
        if (extendedManagedType == null) {
            extendedManagedType = new TemporaryExtendedManagedType(type, new HashMap<String, AttributeEntry<?, ?>>());
            temporaryExtendedManagedTypes.put(type.getJavaType().getName(), extendedManagedType);
        }

        for (Attribute<?, ?> attribute : attributes) {
            ArrayList<Attribute<?, ?>> newParents = new ArrayList<>(parents.size() + 1);
            newParents.addAll(parents);
            newParents.add(attribute);
            String attributeName = parent + "." + attribute.getName();
            Class<?> fieldType = JpaMetamodelUtils.resolveFieldClass(type.getJavaType(), attribute);
            if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                collectColumnNames(e, attributeMap, attributeName, newParents, delegate.embeddable(fieldType), temporaryExtendedManagedTypes);
            } else if (isAssociation(attribute) && !attribute.isCollection() && !jpaProvider.isForeignJoinColumn(e, attributeName)) {
                // We create an attribute entry for the id attribute of *ToOne relations if the columns reside on the Many side
                List<String> identifierOrUniqueKeyEmbeddedPropertyNames = jpaProvider.getIdentifierOrUniqueKeyEmbeddedPropertyNames(e, attributeName);

                for (String name : identifierOrUniqueKeyEmbeddedPropertyNames) {
                    EntityType<?> fieldEntityType = delegate.entity(fieldType);
                    Attribute<?, ?> idAttribute = JpaMetamodelUtils.getAttribute(fieldEntityType, name);
                    Class<?> idType = JpaMetamodelUtils.resolveFieldClass(fieldType, idAttribute);
                    String idPath = attributeName + "." + name;
                    ArrayList<Attribute<?, ?>> idParents = new ArrayList<>(newParents.size() + 1);
                    idParents.addAll(newParents);
                    idParents.add(idAttribute);
                    attributeMap.put(idPath, new AttributeEntry(jpaProvider, e, idAttribute, idPath, idType, idParents));
                }
            }

            AttributeEntry attributeEntry = new AttributeEntry(jpaProvider, e, attribute, attributeName, fieldType, newParents);
            attributeMap.put(attributeName, attributeEntry);
            extendedManagedType.attributes.put(attribute.getName(), attributeEntry);
        }
    }

    private static boolean isAssociation(Attribute<?, ?> attribute) {
        return attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE || attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE;
    }

    public JpaProvider getJpaProvider() {
        return jpaProvider;
    }

    @Override
    public <X> EntityType<X> entity(Class<X> cls) {
        return delegate.entity(cls);
    }

    public EntityType<?> entity(String name) {
        EntityType<?> type = entityNameMap.get(name);
        if (type == null) {
            throw new IllegalArgumentException("Invalid entity type: " + name);
        }
        return type;
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
    @SuppressWarnings({ "unchecked" })
    public <X> Type<X> type(Class<X> cls) {
        Type<?> type = classMap.get(cls);
        if (type != null) {
            return (Type<X>) type;
        }

        type = new BasicTypeImpl<>(cls);
        Type<?> oldType = basicTypeMap.putIfAbsent(cls, type);
        if (oldType != null) {
            type = oldType;
        }
        return (Type<X>) type;
    }

    @Override
    public ManagedType<?> managedType(String name) {
        ManagedType<?> t = entityNameMap.get(name);
        if (t == null) {
            throw new IllegalStateException("Managed type with name '" + name + "' does not exist!");
        }

        return t;
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public <X> ManagedType<X> getManagedType(Class<X> cls) {
        return (ManagedType<X>) classMap.get(cls);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public <X> EntityType<X> getEntity(Class<X> cls) {
        Type<?> type = classMap.get(cls);
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

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getManagedType(Class<T> cls, ManagedType<?> managedType) {
        if (managedType.getJavaType() == null) {
            return getManagedType(cls, JpaMetamodelUtils.getTypeName(managedType));
        } else {
            return getManagedType(cls, managedType.getJavaType());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getManagedType(Class<T> cls, Class<?> managedType) {
        ExtendedManagedType<?> extendedManagedType = getEntry(managedType);
        if (cls == ExtendedManagedType.class) {
            return (T) extendedManagedType;
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getManagedType(Class<T> cls, String managedTypeName) {
        ExtendedManagedType<?> extendedManagedType = getEntry(managedTypeName);
        if (cls == ExtendedManagedType.class) {
            return (T) extendedManagedType;
        }
        return null;
    }

    private ExtendedManagedType<?> getEntry(Class<?> ownerType) {
        ExtendedManagedType<?> extendedManagedType = extendedManagedTypes.get(ownerType);
        if (extendedManagedType == null) {
            throw new IllegalArgumentException("Unknown managed type '" + ownerType.getName() + "'");
        }
        return extendedManagedType;
    }

    private ExtendedManagedType<?> getEntry(String managedTypeName) {
        ExtendedManagedType<?> extendedManagedType = extendedManagedTypes.get(managedTypeName);
        if (extendedManagedType == null) {
            throw new IllegalArgumentException("Unknown managed type '" + managedTypeName + "'");
        }
        return extendedManagedType;
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static final class TemporaryExtendedManagedType {
        private final ManagedType<?> managedType;
        private final Map<String, AttributeEntry<?, ?>> attributes;
        private boolean done;
        private boolean cascadingDeleteCycle;

        private TemporaryExtendedManagedType(ManagedType<?> managedType, Map<String, AttributeEntry<?, ?>> attributes) {
            this.managedType = managedType;
            this.attributes = attributes;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static final class ExtendedManagedTypeImpl<X> implements ExtendedManagedType<X> {
        private final ManagedType<X> managedType;
        private final boolean hasCascadingDeleteCycle;
        private final Set<SingularAttribute<X, ?>> idAttributes;
        private final Map<String, AttributeEntry<?, ?>> attributes;

        @SuppressWarnings("unchecked")
        private ExtendedManagedTypeImpl(ManagedType<X> managedType, boolean hasCascadingDeleteCycle, Map<String, AttributeEntry<?, ?>> attributes) {
            this.managedType = managedType;
            if (managedType instanceof EntityType<?>) {
                this.idAttributes = (Set<SingularAttribute<X, ?>>) (Set) JpaMetamodelUtils.getIdAttributes((IdentifiableType<?>) managedType);
            } else {
                this.idAttributes = Collections.emptySet();
            }
            this.hasCascadingDeleteCycle = hasCascadingDeleteCycle;
            this.attributes = attributes;
        }

        @Override
        public ManagedType<X> getType() {
            return managedType;
        }

        @Override
        public boolean hasCascadingDeleteCycle() {
            return hasCascadingDeleteCycle;
        }

        @Override
        public SingularAttribute<X, ?> getIdAttribute() {
            Iterator<SingularAttribute<X, ?>> iterator = idAttributes.iterator();
            if (iterator.hasNext()) {
                SingularAttribute<X, ?> idAttribute = iterator.next();
                if (iterator.hasNext()) {
                    throw new IllegalStateException("Can't access a single id attribute as the entity has multiple id attributes i.e. uses @IdClass!");
                }
                return idAttribute;
            }
            return null;
        }

        @Override
        public Set<SingularAttribute<X, ?>> getIdAttributes() {
            return idAttributes;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Map<String, ExtendedAttribute<X, ?>> getAttributes() {
            return (Map<String, ExtendedAttribute<X, ?>>) (Map<?, ?>) attributes;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ExtendedAttribute<X, ?> getAttribute(String attributeName) {
            AttributeEntry<?, ?> entry = attributes.get(attributeName);
            if (entry == null) {
                throw new IllegalArgumentException("Could not find attribute '" + attributeName + "' on managed type '" + managedType.getJavaType().getName() + "'");
            }
            return (ExtendedAttribute<X, ?>) entry;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static final class AttributeEntry<X, Y> implements ExtendedAttribute<X, Y> {

        private static final Map<String, String> NO_MAPPINGS = new HashMap<>();

        private final JpaProvider jpaProvider;
        private final ManagedType<?> ownerType;
        private final Attribute<X, Y> attribute;
        private final List<Attribute<?, ?>> attributePath;
        private final Class<Y> elementClass;
        private final boolean hasCascadeDeleteCycle;
        private final boolean isForeignJoinColumn;
        private final boolean isColumnShared;
        private final boolean isBag;
        private final boolean isOrphanRemoval;
        private final boolean isDeleteCascaded;
        private final JpaProvider.ConstraintType[] joinTypeIndexedRequiresTreatFilter;
        private final String mappedBy;
        private final JoinTable joinTable;
        private final String[] columnNames;
        private final String[] columnTypes;
        private final ConcurrentMap<EntityType<?>, Map<String, String>> inverseAttributeCache = new ConcurrentHashMap<>();
        private final Set<ExtendedAttribute<X, ?>> columnEquivalentAttributes = new HashSet<>();

        public AttributeEntry(JpaProvider jpaProvider, ManagedType<X> ownerType, Attribute<X, Y> attribute, String attributeName, Class<Y> fieldType, List<Attribute<?, ?>> parents) {
            this.jpaProvider = jpaProvider;
            this.ownerType = ownerType;
            this.attribute = attribute;

            this.attributePath = Collections.unmodifiableList(parents);
            this.elementClass = fieldType;
            this.isOrphanRemoval = jpaProvider.isOrphanRemoval(ownerType, attributeName);
            this.isDeleteCascaded = jpaProvider.isDeleteCascaded(ownerType, attributeName);
            this.hasCascadeDeleteCycle = false;
            JoinType[] joinTypes = JoinType.values();
            JpaProvider.ConstraintType[] requiresTreatFilter = new JpaProvider.ConstraintType[joinTypes.length];
            if (ownerType instanceof EntityType<?>) {
                EntityType<?> entityType = (EntityType<?>) ownerType;
                this.isForeignJoinColumn = jpaProvider.isForeignJoinColumn(entityType, attributeName);
                this.isColumnShared = jpaProvider.isColumnShared(entityType, attributeName);
                this.isBag = jpaProvider.isBag(entityType, attributeName);
                this.mappedBy = jpaProvider.getMappedBy(entityType, attributeName);
                this.joinTable = jpaProvider.getJoinTable(entityType, attributeName);
                this.columnNames = jpaProvider.getColumnNames(entityType, attributeName);
                this.columnTypes = jpaProvider.getColumnTypes(entityType, attributeName);
                for (JoinType joinType : joinTypes) {
                    requiresTreatFilter[joinType.ordinal()] = jpaProvider.requiresTreatFilter(entityType, attributeName, joinType);
                }
            } else {
                this.isForeignJoinColumn = false;
                this.isColumnShared = false;
                this.isBag = false;
                this.mappedBy = null;
                this.joinTable = null;
                this.columnNames = null;
                this.columnTypes = null;
            }
            this.joinTypeIndexedRequiresTreatFilter = requiresTreatFilter;
        }

        private AttributeEntry(AttributeEntry<X, Y> original, boolean hasCascadeDeleteCycle) {
            this.jpaProvider = original.jpaProvider;
            this.ownerType = original.ownerType;
            this.attribute = original.attribute;
            this.attributePath = original.attributePath;
            this.elementClass = original.elementClass;
            this.hasCascadeDeleteCycle = hasCascadeDeleteCycle;
            this.isForeignJoinColumn = original.isForeignJoinColumn;
            this.isColumnShared = original.isColumnShared;
            this.isBag = original.isBag;
            this.isOrphanRemoval = original.isOrphanRemoval;
            this.isDeleteCascaded = original.isDeleteCascaded;
            this.joinTypeIndexedRequiresTreatFilter = original.joinTypeIndexedRequiresTreatFilter;
            this.mappedBy = original.mappedBy;
            this.joinTable = original.joinTable;
            this.columnNames = original.columnNames;
            this.columnTypes = original.columnTypes;
        }

        @Override
        public Map<String, String> getWritableMappedByMappings(EntityType<?> inverseType) {
            Map<String, String> mappings = inverseAttributeCache.get(inverseType);
            if (mappings == null) {
                mappings = jpaProvider.getWritableMappedByMappings(inverseType, (EntityType<?>) ownerType, attribute.getName());
                if (mappings == null) {
                    inverseAttributeCache.putIfAbsent(inverseType, NO_MAPPINGS);
                } else {
                    mappings = Collections.unmodifiableMap(mappings);
                    inverseAttributeCache.putIfAbsent(inverseType, mappings);
                }
            } else if (mappings == NO_MAPPINGS) {
                // Special value to indicate "no mappings"
                return null;
            }
            return mappings;
        }

        @Override
        public Attribute<X, Y> getAttribute() {
            return attribute;
        }

        @Override
        public List<Attribute<?, ?>> getAttributePath() {
            return attributePath;
        }

        @Override
        public Class<Y> getElementClass() {
            return elementClass;
        }

        @Override
        public boolean hasCascadingDeleteCycle() {
            return hasCascadeDeleteCycle;
        }

        @Override
        public boolean isForeignJoinColumn() {
            return isForeignJoinColumn;
        }

        @Override
        public boolean isColumnShared() {
            return isColumnShared;
        }

        @Override
        public boolean isBag() {
            return isBag;
        }

        @Override
        public boolean isOrphanRemoval() {
            return isOrphanRemoval;
        }

        @Override
        public boolean isDeleteCascaded() {
            return isDeleteCascaded;
        }

        @Override
        public JpaProvider.ConstraintType getJoinTypeIndexedRequiresTreatFilter(JoinType joinType) {
            return joinTypeIndexedRequiresTreatFilter[joinType.ordinal()];
        }

        @Override
        public String getMappedBy() {
            return mappedBy;
        }

        @Override
        public JoinTable getJoinTable() {
            return joinTable;
        }

        @Override
        public String[] getColumnNames() {
            return columnNames;
        }

        @Override
        public String[] getColumnTypes() {
            return columnTypes;
        }

        @Override
        public Set<ExtendedAttribute<X, ?>> getColumnEquivalentAttributes() {
            return columnEquivalentAttributes;
        }

        public void initColumnEquivalentAttributes(Collection<AttributeEntry<?, ?>> attributeEntries) {
            for (AttributeEntry<?, ?> attributeEntry : attributeEntries) {
                if (attributeEntry != this && Arrays.equals(columnNames, attributeEntry.columnNames)) {
                    columnEquivalentAttributes.add((ExtendedAttribute<X, ?>) attributeEntry);
                }
            }
        }

        public AttributeEntry<X, Y> withCascadingDeleteCycle() {
            if (hasCascadeDeleteCycle) {
                return this;
            }
            return new AttributeEntry<>(this, true);
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static class BasicTypeImpl<T> implements BasicType<T> {

        private final Class<T> cls;

        public BasicTypeImpl(Class<T> cls) {
            this.cls = cls;
        }

        @Override
        public PersistenceType getPersistenceType() {
            return PersistenceType.BASIC;
        }

        @Override
        public Class<T> getJavaType() {
            return cls;
        }
    }
}
