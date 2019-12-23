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

package com.blazebit.persistence.view.impl.update;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spi.JoinTable;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.OptimisticLockException;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.MacroConfigurationExpressionFactory;
import com.blazebit.persistence.view.impl.accessor.Accessors;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.accessor.InitialValueAttributeAccessor;
import com.blazebit.persistence.view.impl.change.DirtyChecker;
import com.blazebit.persistence.view.impl.collection.CollectionInstantiator;
import com.blazebit.persistence.view.impl.collection.CollectionRemoveListener;
import com.blazebit.persistence.view.impl.collection.MapInstantiator;
import com.blazebit.persistence.view.impl.collection.RecordingList;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.impl.entity.CreateOnlyViewToEntityMapper;
import com.blazebit.persistence.view.impl.entity.EmbeddableUpdaterBasedViewToEntityMapper;
import com.blazebit.persistence.view.impl.entity.EntityIdLoader;
import com.blazebit.persistence.view.impl.entity.EntityLoader;
import com.blazebit.persistence.view.impl.entity.EntityTupleizer;
import com.blazebit.persistence.view.impl.entity.FullEntityLoader;
import com.blazebit.persistence.view.impl.entity.LoadOrPersistViewToEntityMapper;
import com.blazebit.persistence.view.impl.entity.MapViewToEntityMapper;
import com.blazebit.persistence.view.impl.entity.ReferenceEntityLoader;
import com.blazebit.persistence.view.impl.entity.UpdaterBasedViewToEntityMapper;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.macro.MutableEmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.impl.mapper.Mapper;
import com.blazebit.persistence.view.impl.mapper.Mappers;
import com.blazebit.persistence.view.impl.mapper.ViewMapper;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.BasicTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.ViewTypeImplementor;
import com.blazebit.persistence.view.impl.proxy.DirtyStateTrackable;
import com.blazebit.persistence.view.impl.proxy.MutableStateTrackable;
import com.blazebit.persistence.view.impl.update.flush.BasicAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.CollectionAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.CompositeAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.DirtyAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.EmbeddableAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.EntityCollectionRemoveListener;
import com.blazebit.persistence.view.impl.update.flush.FetchGraphNode;
import com.blazebit.persistence.view.impl.update.flush.IndexedListAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.InverseFlusher;
import com.blazebit.persistence.view.impl.update.flush.MapAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.SimpleMapViewToEntityMapper;
import com.blazebit.persistence.view.impl.update.flush.SubviewAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.TypeDescriptor;
import com.blazebit.persistence.view.impl.update.flush.UnmappedAttributeCascadeDeleter;
import com.blazebit.persistence.view.impl.update.flush.UnmappedBasicAttributeCascadeDeleter;
import com.blazebit.persistence.view.impl.update.flush.UnmappedCollectionAttributeCascadeDeleter;
import com.blazebit.persistence.view.impl.update.flush.UnmappedMapAttributeCascadeDeleter;
import com.blazebit.persistence.view.impl.update.flush.UnmappedWritableBasicAttributeSetNullCascadeDeleter;
import com.blazebit.persistence.view.impl.update.flush.VersionAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.ViewCollectionRemoveListener;
import com.blazebit.persistence.view.metamodel.BasicType;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;
import com.blazebit.persistence.view.spi.type.VersionBasicUserType;

import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityViewUpdaterImpl implements EntityViewUpdater {

    public static final String WHERE_CLAUSE_PREFIX = "_";

    private final boolean fullFlush;
    private final boolean rootUpdateAllowed;
    private final ManagedViewTypeImplementor<?> managedViewType;
    private final FlushStrategy flushStrategy;
    private final EntityLoader fullEntityLoader;
    private final DirtyAttributeFlusher<?, Object, Object> idFlusher;
    private final VersionAttributeFlusher<Object, Object> versionFlusher;
    private final CompositeAttributeFlusher fullFlusher;
    private final String updatePrefixString;
    private final String updatePostfixString;
    private final String fullUpdateQueryString;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public EntityViewUpdaterImpl(EntityViewManagerImpl evm, ManagedViewTypeImplementor<?> viewType, ManagedViewTypeImplementor<?> declaredViewType, EntityViewUpdaterImpl owner, String ownerMapping) {
        evm.addUpdater(viewType, declaredViewType, owner, ownerMapping, this);
        Class<?> entityClass = viewType.getEntityClass();
        this.managedViewType = viewType;
        this.fullFlush = viewType.getFlushMode() == FlushMode.FULL;
        this.flushStrategy = viewType.getFlushStrategy();
        EntityMetamodel entityMetamodel = evm.getMetamodel().getEntityMetamodel();
        ExtendedManagedType<?> extendedManagedType = entityMetamodel.getManagedType(ExtendedManagedType.class, entityClass);
        EntityType<?> entityType = extendedManagedType.getType() instanceof EntityType<?> ? (EntityType<?>) extendedManagedType.getType() : null;
        ViewToEntityMapper viewIdMapper = null;

        final AttributeAccessor viewIdAccessor;
        final EntityTupleizer tupleizer;
        final ObjectBuilder<Object> idViewBuilder;
        final EntityLoader jpaIdInstantiator;
        boolean persistable = entityType != null;
        ViewMapper<Object, Object> persistViewMapper;
        if (persistable && viewType instanceof ViewType<?>) {
            // To be able to invoke EntityViewManager#update on an updatable view of this type, it must have an id i.e. be a ViewType instead of a FlatViewType
            // Otherwise we can't load the object itself
            ViewType<?> view = (ViewType<?>) viewType;
            this.rootUpdateAllowed = true;
            viewIdAccessor = Accessors.forViewId(evm, (ViewType<?>) viewType, false);
            com.blazebit.persistence.view.metamodel.SingularAttribute<?, ?> viewIdAttribute = (com.blazebit.persistence.view.metamodel.SingularAttribute<?, ?>) view.getIdAttribute();
            if (view.getIdAttribute().isSubview()) {
                ManagedViewTypeImplementor<?> viewIdType = (ManagedViewTypeImplementor<?>) viewIdAttribute.getType();
                boolean updateMappable = isUpdateMappable((Set) viewIdType.getAttributes());
                if (updateMappable) {
                    viewIdMapper = createViewIdMapper(evm, view);
                    tupleizer = new DefaultEntityTupleizer(evm, viewIdType);
                    ExpressionFactory ef = evm.getCriteriaBuilderFactory().getService(ExpressionFactory.class);
                    idViewBuilder = (ObjectBuilder<Object>) evm.getTemplate(
                            new MacroConfigurationExpressionFactory(ef, ef.getDefaultMacroConfiguration()),
                            viewIdType,
                            null,
                            null,
                            null,
                            new MutableEmbeddingViewJpqlMacro(),
                            0
                    ).createObjectBuilder(null, null, null, 0);
                    jpaIdInstantiator = null;
                } else {
                    tupleizer = null;
                    idViewBuilder = null;
                    jpaIdInstantiator = null;
                }
            } else {
                tupleizer = null;
                idViewBuilder = null;
                jpaIdInstantiator = ((BasicTypeImpl<?>) viewIdAttribute.getType()).isJpaManaged() ? new EntityIdLoader(viewIdAttribute.getJavaType()) : null;
            }
            persistViewMapper = declaredViewType != null ? (ViewMapper<Object, Object>) evm.getViewMapper(new ViewMapper.Key(viewType, declaredViewType, false, false)) : null;
            this.idFlusher = createIdFlusher(evm, view, viewIdMapper);
        } else {
            this.rootUpdateAllowed = false;
            viewIdAccessor = null;
            tupleizer = null;
            idViewBuilder = null;
            jpaIdInstantiator = null;
            persistViewMapper = null;
            this.idFlusher = null;
        }
        boolean mutable = viewType.isCreatable() || viewType.isUpdatable();
        this.fullEntityLoader = mutable ? new FullEntityLoader(evm, viewType) : null;

        Set<MethodAttribute<?, ?>> attributes = (Set<MethodAttribute<?, ?>>) (Set<?>) viewType.getAttributes();
        String idAttributeName = null;
        javax.persistence.metamodel.SingularAttribute<?, ?> jpaIdAttribute = null;
        javax.persistence.metamodel.SingularAttribute<?, ?> viewIdMappingAttribute = null;
        AbstractMethodAttribute<?, ?> idAttribute;
        AbstractMethodAttribute<?, ?> versionAttribute;

        if (viewType instanceof ViewType<?>) {
            idAttribute = (AbstractMethodAttribute<?, ?>) ((ViewType) viewType).getIdAttribute();
            versionAttribute = (AbstractMethodAttribute<?, ?>) ((ViewType) viewType).getVersionAttribute();
            versionFlusher = versionAttribute != null ? createVersionFlusher(evm, entityType, versionAttribute) : null;
            jpaIdAttribute = extendedManagedType.getIdAttribute();
            idAttributeName = jpaIdAttribute.getName();
            String mapping = idAttribute.getMapping();
            ExtendedAttribute<?, ?> extendedAttribute = extendedManagedType.getAttributes().get(mapping);
            viewIdMappingAttribute = extendedAttribute == null ? null : (SingularAttribute<?, ?>) extendedAttribute.getAttribute();
            // Read only entity views don't have this restriction
            if ((viewType.isCreatable() || viewType.isUpdatable()) && !mapping.equals(jpaIdAttribute.getName())) {
                throw new IllegalArgumentException("Expected JPA id attribute [" + jpaIdAttribute.getName() + "] to match the entity view id attribute mapping [" + mapping + "] but it didn't!");
            }
        } else {
            idAttribute = null;
            versionAttribute = null;
            versionFlusher = null;
        }

        // Flushers are ordered like the dirty and initial state array and have matching indexes
        // Since attributes for which pass through flushers are created are not updatable and not mutable, they must come last, as there is no dirty state index for them
        List<DirtyAttributeFlusher<?, ?, ?>> flushers = new ArrayList<>(attributes.size());
        List<DirtyAttributeFlusher<?, ?, ?>> passThroughFlushers = null;

        StringBuilder sb = null;
        int clauseEndIndex = -1;

        if (mutable && flushStrategy != FlushStrategy.ENTITY && jpaIdAttribute != null) {
            this.updatePrefixString = "UPDATE " + entityType.getName() + " e SET ";
            StringBuilder tmpSb = new StringBuilder();
            tmpSb.append(" WHERE ");
            idFlusher.appendUpdateQueryFragment(null, tmpSb, "e.", WHERE_CLAUSE_PREFIX, " AND ");
            if (versionAttribute != null) {
                tmpSb.append(" AND ");
                versionFlusher.appendUpdateQueryFragment(null, tmpSb, "e.", WHERE_CLAUSE_PREFIX, " AND ");
            }
            this.updatePostfixString = tmpSb.toString();
            sb = new StringBuilder(updatePrefixString.length() + updatePostfixString.length() + attributes.size() * 50);
            sb.append(updatePrefixString);
            clauseEndIndex = sb.length();
        } else {
            this.updatePrefixString = null;
            this.updatePostfixString = null;
        }

        if (versionFlusher != null && sb != null) {
            versionFlusher.appendUpdateQueryFragment(null, sb, null, null, ", ");
            // If something was appended, we also append a comma
            if (clauseEndIndex != sb.length()) {
                clauseEndIndex = sb.length();
                sb.append(", ");
            }
        }

        UnmappedAttributeCascadeDeleter[] cascadeDeleteUnmappedFlushers = null;
        UnmappedAttributeCascadeDeleter[][] flusherWiseCascadeDeleteUnmappedFlushers = null;
        // Exclude it and version attributes from unmapped attributes as they can't have join tables
        TreeMap<String, ? extends ExtendedAttribute<?, ?>> joinTableUnmappedEntityAttributes = new TreeMap<>(extendedManagedType.getOwnedAttributes());
        if (jpaIdAttribute != null) {
            joinTableUnmappedEntityAttributes.remove(jpaIdAttribute.getName());
        }
        if (versionAttribute != null) {
            joinTableUnmappedEntityAttributes.remove(versionAttribute.getMapping());
        }

        // Only construct attribute flushers for creatable, updatable entity views or flat views
        if (mutable || entityType == null) {
            // Create flushers for mapped attributes
            for (MethodAttribute<?, ?> attribute : attributes) {
                if (attribute == idAttribute || attribute == versionAttribute) {
                    continue;
                }
                AbstractMethodAttribute<?, ?> methodAttribute = (AbstractMethodAttribute<?, ?>) attribute;
                // Skip attributes that aren't update mappable
                if (!methodAttribute.isUpdateMappable()) {
                    continue;
                }

                // Exclude mapped attributes
                if (methodAttribute.getMapping() != null) {
                    joinTableUnmappedEntityAttributes.remove(methodAttribute.getMapping());
                }
                DirtyAttributeFlusher flusher = createAttributeFlusher(evm, viewType, idAttributeName, flushStrategy, methodAttribute, idFlusher, owner, ownerMapping);
                if (flusher != null) {
                    if (sb != null) {
                        int endIndex = sb.length();
                        flusher.appendUpdateQueryFragment(null, sb, null, null, ", ");

                        // If something was appended, we also append a comma
                        if (endIndex != sb.length()) {
                            clauseEndIndex = sb.length();
                            sb.append(", ");
                        }
                    }
                    if (flusher.isPassThrough()) {
                        if (passThroughFlushers == null) {
                            passThroughFlushers = new ArrayList<>();
                        }
                        passThroughFlushers.add(flusher);
                    } else {
                        flushers.add(flusher);
                    }
                }
            }

            if (passThroughFlushers != null) {
                flushers.addAll(passThroughFlushers);
            }
        }

        // Remove attributes that don't have a join table
        Iterator<? extends Map.Entry<String, ? extends ExtendedAttribute<?, ?>>> iterator = joinTableUnmappedEntityAttributes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ? extends ExtendedAttribute<?, ?>> entry = iterator.next();
            ExtendedAttribute attributeEntry = entry.getValue();
            JoinTable joinTable = attributeEntry.getJoinTable();
            if (joinTable == null && !"".equals(attributeEntry.getMappedBy()) && (entityMetamodel.getEntity(attributeEntry.getElementClass()) == null || !attributeEntry.isDeleteCascaded())) {
                iterator.remove();
            }
        }

        // Create cascade delete flushers for unmapped attributes
        // Note that some deleters are re-routed to their respective flushers that really "own" them to support cascading and orphan removal in embeddables
        if (!joinTableUnmappedEntityAttributes.isEmpty()) {
            List<UnmappedAttributeCascadeDeleter> cascadeDeleteUnmappedFlusherList = new ArrayList<>(joinTableUnmappedEntityAttributes.size());
            flusherWiseCascadeDeleteUnmappedFlushers = new UnmappedAttributeCascadeDeleter[flushers.size()][];
            Map<String, List<UnmappedAttributeCascadeDeleter>> flusherWiseCascadeDeleteUnmappedFlusherList = new LinkedHashMap<>(flushers.size());

            for (int i = 0; i < flushers.size(); i++) {
                flusherWiseCascadeDeleteUnmappedFlusherList.put(flushers.get(i).getAttributeName(), new ArrayList<UnmappedAttributeCascadeDeleter>());
            }

            for (Map.Entry<String, ? extends ExtendedAttribute<?, ?>> entry : joinTableUnmappedEntityAttributes.entrySet()) {
                String unmappedAttributeName = entry.getKey();
                ExtendedAttribute extendedAttribute = entry.getValue();
                UnmappedAttributeCascadeDeleter deleter;
                if (extendedAttribute.getAttribute().isCollection()) {
                    if ("".equals(extendedAttribute.getMappedBy())) {
                        ExtendedManagedType managedType = entityMetamodel.getManagedType(ExtendedManagedType.class, extendedAttribute.getElementClass());
                        deleter = new UnmappedWritableBasicAttributeSetNullCascadeDeleter(evm, entityType, managedType, extendedAttribute.getWritableMappedByMappings((EntityType<?>) managedType.getType()));
                    } else {
                        if (((javax.persistence.metamodel.PluralAttribute<?, ?, ?>) extendedAttribute.getAttribute()).getCollectionType() == javax.persistence.metamodel.PluralAttribute.CollectionType.MAP) {
                            deleter = new UnmappedMapAttributeCascadeDeleter(evm, unmappedAttributeName, extendedAttribute, entityClass, idAttributeName, false);
                        } else {
                            deleter = new UnmappedCollectionAttributeCascadeDeleter(evm, unmappedAttributeName, extendedAttribute, entityClass, idAttributeName, false);
                        }
                    }
                } else {
                    if ("".equals(extendedAttribute.getMappedBy())) {
                        ExtendedManagedType managedType = entityMetamodel.getManagedType(ExtendedManagedType.class, extendedAttribute.getElementClass());
                        deleter = new UnmappedWritableBasicAttributeSetNullCascadeDeleter(evm, entityType, managedType, extendedAttribute.getWritableMappedByMappings((EntityType<?>) managedType.getType()));
                    } else {
                        deleter = new UnmappedBasicAttributeCascadeDeleter(evm, unmappedAttributeName, extendedAttribute, idAttributeName, false);
                    }
                }

                cascadeDeleteUnmappedFlusherList.add(deleter);

                int dotIndex = unmappedAttributeName.indexOf('.');
                if (dotIndex != -1) {
                    unmappedAttributeName = unmappedAttributeName.substring(0, dotIndex);
                }
                List<UnmappedAttributeCascadeDeleter> flusherWiseDeleters;
                if ((flusherWiseDeleters = flusherWiseCascadeDeleteUnmappedFlusherList.get(unmappedAttributeName)) != null) {
                    flusherWiseDeleters.add(deleter.createFlusherWiseDeleter());
                }
            }

            cascadeDeleteUnmappedFlushers = cascadeDeleteUnmappedFlusherList.toArray(new UnmappedAttributeCascadeDeleter[cascadeDeleteUnmappedFlusherList.size()]);

            int i = 0;
            for (List<UnmappedAttributeCascadeDeleter> tmp : flusherWiseCascadeDeleteUnmappedFlusherList.values()) {
                flusherWiseCascadeDeleteUnmappedFlushers[i++] = tmp.toArray(new UnmappedAttributeCascadeDeleter[tmp.size()]);
            }
        }

        this.fullFlusher = new CompositeAttributeFlusher(
                evm,
                viewType.getJavaType(),
                viewType.getEntityClass(),
                viewType.getJpaManagedType(),
                persistable,
                persistViewMapper,
                jpaIdAttribute,
                viewIdMappingAttribute,
                evm.getEntityIdAccessor(),
                viewIdMapper,
                viewIdAccessor,
                tupleizer,
                jpaIdInstantiator,
                idViewBuilder,
                idFlusher,
                versionFlusher,
                cascadeDeleteUnmappedFlushers,
                flusherWiseCascadeDeleteUnmappedFlushers,
                flushers.toArray(new DirtyAttributeFlusher[flushers.size()]),
                viewType.getFlushMode(),
                flushStrategy
        );
        if (mutable && flushStrategy != FlushStrategy.ENTITY && jpaIdAttribute != null && clauseEndIndex != sb.length()) {
            if (clauseEndIndex + 2 == sb.length()) {
                // Remove the last comma
                sb.setLength(clauseEndIndex);
            }
            sb.append(updatePostfixString);
            this.fullUpdateQueryString = sb.toString();
        } else {
            this.fullUpdateQueryString = null;
        }
    }

    private static boolean isUpdateMappable(Set<AbstractMethodAttribute<?, ?>> attributes) {
        for (AbstractMethodAttribute<?, ?> attribute : attributes) {
            if (!attribute.isUpdateMappable()) {
                return false;
            }
            Type<?> type = ((com.blazebit.persistence.view.metamodel.SingularAttribute<?, ?>) attribute).getType();
            if (type instanceof ManagedViewType<?>) {
                if (!isUpdateMappable(((ManagedViewType) type).getAttributes())) {
                    return false;
                }
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    public static ViewToEntityMapper createViewIdMapper(EntityViewManagerImpl evm, ManagedViewType<?> viewType) {
        if (viewType instanceof ViewType<?>) {
            return createViewIdMapper(evm, (com.blazebit.persistence.view.metamodel.SingularAttribute<?, ?>) ((ViewType<Object>) viewType).getIdAttribute());
        }
        return null;
    }

    public static ViewToEntityMapper createViewIdMapper(EntityViewManagerImpl evm, com.blazebit.persistence.view.metamodel.SingularAttribute<?, ?> viewIdAttribute) {
        if (!viewIdAttribute.isSubview()) {
            return null;
        }
        ManagedViewTypeImplementor<?> viewIdType = (ManagedViewTypeImplementor<?>) viewIdAttribute.getType();

        return new EmbeddableUpdaterBasedViewToEntityMapper(
                ((AbstractMethodAttribute<?, ?>) viewIdAttribute).getLocation(),
                evm,
                viewIdType.getJavaType(),
                Collections.<Type<?>>singleton(viewIdType),
                Collections.<Type<?>>singleton(viewIdType),
                Collections.<Type<?>>emptySet(),
                new ReferenceEntityLoader(evm, viewIdType, null),
                true,
                viewIdType.isUpdatable() ? null : (Mapper<Object, Object>) Mappers.forViewToEntityAttributeMapping(evm, viewIdType, viewIdType.getEntityClass()),
                null,
                null
        );
    }

    public static DirtyAttributeFlusher<?, Object, Object> createIdFlusher(EntityViewManagerImpl evm, ViewType<?> viewType, ViewToEntityMapper viewToEntityMapper) {
        return createIdFlusher(evm, viewType, viewToEntityMapper, (AbstractMethodAttribute<?, ?>) viewType.getIdAttribute());
    }

    public static DirtyAttributeFlusher<?, Object, Object> createIdFlusher(EntityViewManagerImpl evm, ViewType<?> viewType, ViewToEntityMapper viewToEntityMapper, AbstractMethodAttribute<?, ?> idAttribute) {
        String attributeName = idAttribute.getName();
        String attributeMapping = idAttribute.getMapping();
        AttributeAccessor viewAttributeAccessor = Accessors.forViewAttribute(evm, idAttribute, true);
        AttributeAccessor entityAttributeAccessor = Accessors.forEntityMapping(evm, idAttribute);
        CompositeAttributeFlusher nestedFlusher = viewToEntityMapper != null ? (CompositeAttributeFlusher) viewToEntityMapper.getFullGraphNode() : null;
        boolean supportsQueryFlush = nestedFlusher == null || nestedFlusher.supportsQueryFlush() && evm.getJpaProvider().supportsUpdateSetEmbeddable();

        String parameterName;
        String updateFragment;
        if (supportsQueryFlush) {
            parameterName = attributeName;
            updateFragment = attributeMapping;
        } else {
            parameterName = attributeName + "_";
            updateFragment = attributeMapping + ".";
        }
        if (idAttribute.isSubview()) {
            if (viewToEntityMapper == null) {
                return null;
            }
            return new EmbeddableAttributeFlusher<>(
                    attributeName,
                    attributeMapping,
                    updateFragment,
                    parameterName,
                    false,
                    false,
                    supportsQueryFlush,
                    entityAttributeAccessor,
                    viewAttributeAccessor,
                    (EmbeddableUpdaterBasedViewToEntityMapper) viewToEntityMapper
            );
        } else {
            BasicTypeImpl<?> type = (BasicTypeImpl<?>) ((com.blazebit.persistence.view.metamodel.SingularAttribute<?, ?>) idAttribute).getType();
            // If the id is an embedded id, we need to flush individual components for subviews, so we need to determine the component flushers
            Map.Entry<AttributeAccessor, BasicAttributeFlusher>[] componentFlusherEntries = null;
            if (type.isJpaManaged()) {
                Set<Attribute<?, ?>> attributes = (Set) type.getManagedType().getAttributes();
                Map<AttributeAccessor, BasicAttributeFlusher> componentFlushers = new HashMap<>(attributes.size());
                buildComponentFlushers(evm, viewType.getEntityClass(), type.getJavaType(), attributeName + "_", attributeMapping + ".", "", attributes, componentFlushers);
                componentFlusherEntries = componentFlushers.entrySet().toArray(new Map.Entry[componentFlushers.size()]);
            }
            TypeDescriptor typeDescriptor = TypeDescriptor.forType(evm, null, idAttribute, type, null, null);
            return new BasicAttributeFlusher<>(attributeName, attributeMapping, true, false, true, false, false, false, componentFlusherEntries, typeDescriptor, updateFragment, parameterName, entityAttributeAccessor, viewAttributeAccessor, null, null, null);
        }
    }

    @SuppressWarnings("unchecked")
    private static void buildComponentFlushers(EntityViewManagerImpl evm, Class<?> entityClass, Class<?> rootType, String attributePrefix, String mappingPrefix, String accessorPrefix, Set<Attribute<?, ?>> attributes, Map<AttributeAccessor, BasicAttributeFlusher> componentFlushers) {
        for (Attribute<?, ?> attribute : attributes) {
            if (!(attribute instanceof SingularAttribute<?, ?>)) {
                throw new IllegalArgumentException("Plural attributes in embeddable types aren't supported yet! Remove attribute " + attribute.getName() + " of type " + attribute.getDeclaringType().getJavaType().getName() + " or use an entity view instead of the embeddable type!");
            }
            SingularAttribute<?, ?> attr = (SingularAttribute<?, ?>) attribute;
            if (attr.getType() instanceof javax.persistence.metamodel.BasicType<?>) {
                String attributeName = attributePrefix + attribute.getName();
                String attributeMapping = mappingPrefix + attribute.getName();
                String parameterName = attributeName;
                String updateFragment = attributeMapping;
                AttributeAccessor attributeAccessor = Accessors.forEntityMapping(evm, rootType, accessorPrefix + attribute.getName());

                componentFlushers.put(attributeAccessor, new BasicAttributeFlusher<>(
                        attributeName,
                        attributeMapping,
                        true,
                        false,
                        true,
                        false,
                        false,
                        false,
                        null,
                        TypeDescriptor.forEntityComponentType(),
                        updateFragment,
                        parameterName,
                        attributeAccessor,
                        null,
                        null,
                        null,
                        null
                ));
            } else {
                ManagedType<?> managedType = (ManagedType<?>) attr.getType();
                Set<Attribute<?, ?>> subAttributes;
                if (managedType instanceof EmbeddableType<?>) {
                    subAttributes = (Set) managedType.getAttributes();
                } else {
                    subAttributes = new HashSet<>();
                    EntityType<?> entity = evm.getMetamodel().getEntityMetamodel().getEntity(entityClass);
                    for (String propertyName : evm.getJpaProvider().getJoinMappingPropertyNames(entity, null, mappingPrefix + attribute.getName()).keySet()) {
                        subAttributes.add(managedType.getAttribute(propertyName));
                    }
                }
                buildComponentFlushers(
                        evm,
                        entityClass,
                        rootType,
                        attributePrefix + attribute.getName() + "_",
                        mappingPrefix + attribute.getName() + ".",
                        accessorPrefix + attribute.getName() + ".",
                        subAttributes,
                        componentFlushers
                );
            }
        }
    }

    private VersionAttributeFlusher<Object, Object> createVersionFlusher(EntityViewManagerImpl evm, EntityType<?> entityType, AbstractMethodAttribute<?, ?> versionAttribute) {
        String attributeName = versionAttribute.getName();
        String attributeMapping = versionAttribute.getMapping();
        String parameterName = versionAttribute.getName();
        String updateFragment = versionAttribute.getMapping();
        AttributeAccessor viewAttributeAccessor = Accessors.forViewAttribute(evm, versionAttribute, false);
        AttributeAccessor attributeAccessor = Accessors.forEntityMapping(evm, versionAttribute);
        Type<?> type = ((com.blazebit.persistence.view.metamodel.SingularAttribute<?, ?>) versionAttribute).getType();
        @SuppressWarnings("unchecked")
        VersionBasicUserType<Object> userType = (VersionBasicUserType<Object>) ((BasicType<?>) type).getUserType();
        boolean jpaVersion = entityType.getSingularAttribute(versionAttribute.getMapping()).isVersion();
        return new VersionAttributeFlusher<>(attributeName, attributeMapping, userType, updateFragment, parameterName, attributeAccessor, viewAttributeAccessor, jpaVersion);
    }

    @Override
    public FetchGraphNode<?> getFullGraphNode() {
        return fullFlusher;
    }

    @Override
    public DirtyAttributeFlusher<?, ?, ?> getIdFlusher() {
        return idFlusher;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DirtyAttributeFlusher<T, E, V>, E, V> DirtyAttributeFlusher<T, E, V> getNestedDirtyFlusher(UpdateContext context, MutableStateTrackable updatableProxy, DirtyAttributeFlusher<T, E, V> fullFlusher) {
        if (context.isForceFull() || fullFlush) {
            if (fullFlusher != null) {
                return fullFlusher;
            } else {
                return (DirtyAttributeFlusher<T, E, V>) this.fullFlusher;
            }
        }

        return this.fullFlusher.getNestedDirtyFlusher(context, updatableProxy);
    }

    @Override
    @SuppressWarnings("unchecked")
    public DirtyChecker<DirtyStateTrackable> getDirtyChecker() {
        return (DirtyChecker<DirtyStateTrackable>) (DirtyChecker<?>) fullFlusher;
    }

    public ManagedViewTypeImplementor<?> getManagedViewType() {
        return managedViewType;
    }

    @Override
    public boolean executeUpdate(UpdateContext context, MutableStateTrackable updatableProxy) {
        return update(context, null, updatableProxy);
    }

    @Override
    public Object executeUpdate(UpdateContext context, Object entity, MutableStateTrackable updatableProxy) {
        if (entity == null) {
            throw new IllegalArgumentException("Illegal null entity!");
        }
        update(context, entity, updatableProxy);
        return entity;
    }

    @Override
    public Query createUpdateQuery(UpdateContext context, MutableStateTrackable updatableProxy, DirtyAttributeFlusher<?, ?, ?> flusher) {
        String queryString;
        boolean needsOptimisticLocking;
        if (flusher == fullFlusher) {
            queryString = fullUpdateQueryString;
            needsOptimisticLocking = fullFlusher.hasVersionFlusher();
        } else {
            StringBuilder sb = new StringBuilder(updatePrefixString.length() + updatePostfixString.length() + 250);
            sb.append(updatePrefixString);
            int initialLength = sb.length();
            flusher.appendUpdateQueryFragment(context, sb, null, null, ", ");
            if (sb.length() == initialLength) {
                // If we still need optimistic locking, we just append a flush for the version increment
                if (needsOptimisticLocking = fullFlusher.hasVersionFlusher() && flusher.isOptimisticLockProtected()) {
                    versionFlusher.appendUpdateQueryFragment(context, sb, null, null, ", ");
                    sb.append(updatePostfixString);
                    queryString = sb.toString();
                } else {
                    queryString = null;
                    needsOptimisticLocking = false;
                }
            } else {
                sb.append(updatePostfixString);
                queryString = sb.toString();
                needsOptimisticLocking = fullFlusher.hasVersionFlusher() && flusher.isOptimisticLockProtected();
            }
        }

        Query query = null;
        if (queryString != null) {
            query = context.getEntityManager().createQuery(queryString);
            if (idFlusher != null) {
                idFlusher.flushQuery(context, WHERE_CLAUSE_PREFIX, null, query, updatableProxy, updatableProxy, updatableProxy.$$_getId(), null);
            }
            if (needsOptimisticLocking) {
                versionFlusher.flushQueryInitialVersion(context, WHERE_CLAUSE_PREFIX, query, updatableProxy, updatableProxy.$$_getVersion());
            }
        }

        return query;
    }

    private boolean update(UpdateContext context, Object entity, MutableStateTrackable updatableProxy) {
        if (!rootUpdateAllowed && entity == null) {
            throw new IllegalArgumentException("Updating instances of the view type [" + updatableProxy.getClass().getName() + "] is not allowed because no entity id is known!");
        }

        @SuppressWarnings("unchecked")
        DirtyAttributeFlusher<?, Object, Object> flusher = getNestedDirtyFlusher(context, updatableProxy, (DirtyAttributeFlusher) null);

        // If nothing is dirty, we don't have to do anything
        if (flusher == null) {
            return false;
        }

        try {
            if (flushStrategy == FlushStrategy.ENTITY || context.isForceEntity() || !flusher.supportsQueryFlush()) {
                return flusher.flushEntity(context, entity, updatableProxy, updatableProxy, updatableProxy, null);
            } else {
                int orphanRemovalStartIndex = context.getOrphanRemovalDeleters().size();
                Query query = flusher.flushQuery(context, null, this, null, updatableProxy, updatableProxy, updatableProxy, null);
                if (query != null) {
                    int updated = query.executeUpdate();

                    if (updated != 1) {
                        throw new OptimisticLockException(entity, updatableProxy);
                    }
                }
                context.removeOrphans(orphanRemovalStartIndex);
                return true;
            }
        } finally {
            context.getInitialStateResetter().addUpdatedView(updatableProxy);
        }
    }

    @Override
    public Object executePersist(UpdateContext context, MutableStateTrackable updatableProxy) {
        Object entity = fullEntityLoader.toEntity(context, updatableProxy, null);
        return executePersist(context, entity, updatableProxy);
    }

    @Override
    public Object executePersist(UpdateContext context, Object entity, MutableStateTrackable updatableProxy) {
        fullFlusher.flushEntity(context, entity, updatableProxy, updatableProxy, updatableProxy, null);
        return entity;
    }

    @Override
    public void remove(UpdateContext context, EntityViewProxy entityView) {
        if (flushStrategy == FlushStrategy.ENTITY) {
            // TODO: pre-load cascade deleted entity graph
        }
        fullFlusher.remove(context, null, entityView, entityView);
    }

    @Override
    public void remove(UpdateContext context, Object viewId) {
        fullFlusher.remove(context, viewId);
    }

    @SuppressWarnings({"unchecked", "checkstyle:methodlength"})
    private DirtyAttributeFlusher<?, ?, ?> createAttributeFlusher(EntityViewManagerImpl evm, ManagedViewTypeImplementor<?> viewType, String idAttributeName, FlushStrategy flushStrategy, AbstractMethodAttribute<?, ?> attribute, DirtyAttributeFlusher<?, ?, ?> ownerIdFlusher, EntityViewUpdaterImpl owner, String ownerMapping) {
        if (attribute.isCollection()) {
            String idMapping;
            if (owner == null) {
                idMapping = idAttributeName;
            } else {
                if (owner.idFlusher instanceof EmbeddableAttributeFlusher<?, ?>) {
                    idMapping = ((EmbeddableAttributeFlusher<Object, Object>) owner.idFlusher).getMapping();
                } else {
                    idMapping = ((BasicAttributeFlusher<Object, Object>) owner.idFlusher).getMapping();
                }
            }
            return createPluralAttributeFlusher(evm, viewType, idMapping, flushStrategy, attribute, owner == null ? ownerIdFlusher : owner.idFlusher, owner, ownerMapping);
        } else {
            return createSingularAttributeFlusher(evm, viewType, attribute, owner, ownerMapping);
        }
    }

    private DirtyAttributeFlusher<?, ?, ?> createPluralAttributeFlusher(EntityViewManagerImpl evm, ManagedViewTypeImplementor<?> viewType, String idAttributeName, FlushStrategy flushStrategy, AbstractMethodAttribute<?, ?> attribute, DirtyAttributeFlusher<?, ?, ?> ownerIdFlusher, EntityViewUpdaterImpl owner, String ownerMapping) {
        EntityMetamodel entityMetamodel = evm.getMetamodel().getEntityMetamodel();
        Class<?> entityClass = viewType.getEntityClass();
        ExtendedManagedType managedType = entityMetamodel.getManagedType(ExtendedManagedType.class, entityClass);
        String attributeName = attribute.getName();
        String attributeMapping = attribute.getMapping();
        AttributeAccessor entityAttributeAccessor = Accessors.forEntityMapping(evm, attribute);
        boolean cascadeDelete = attribute.isDeleteCascaded();
        boolean viewOnlyDeleteCascaded = cascadeDelete && !managedType.getAttribute(attributeMapping).isDeleteCascaded();
        boolean optimisticLockProtected = attribute.isOptimisticLockProtected();
        JpaProvider jpaProvider = evm.getJpaProvider();
        PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
        InitialValueAttributeAccessor viewAttributeAccessor = Accessors.forMutableViewAttribute(evm, attribute);
        ManagedType<?> ownerManagedType = owner == null ? viewType.getJpaManagedType() : owner.managedViewType.getJpaManagedType();
        EntityType<?> ownerEntityType = ownerManagedType instanceof EntityType<?> ? (EntityType<?>) ownerManagedType : null;
        DirtyAttributeFlusher<?, ?, ?> attributeOwnerFlusher;
        if (attributeMapping == null || ownerEntityType == null) {
            // In case of cascading only attributes i.e. correlations, we use the owner id flusher as it's not updatable anyway
            attributeOwnerFlusher = ownerIdFlusher;
        } else {
            Map<String, String> joinTableOwnerProperties;
            if (ownerMapping == null) {
                joinTableOwnerProperties = jpaProvider.getJoinMappingPropertyNames(ownerEntityType, ownerMapping, attributeMapping);
            } else {
                joinTableOwnerProperties = jpaProvider.getJoinMappingPropertyNames(ownerEntityType, ownerMapping, ownerMapping + "." + attributeMapping);
            }
            if (joinTableOwnerProperties.size() != 1) {
                String idMapping = ownerIdFlusher.getMapping();
                String prefix;
                if (idMapping.endsWith(".")) {
                    prefix = idMapping;
                } else {
                    prefix = idMapping + ".";
                }
                for (String joinTableOwnerProperty : joinTableOwnerProperties.keySet()) {
                    if (!joinTableOwnerProperty.startsWith(prefix)) {
                        throw new IllegalArgumentException("Multiple joinable owner properties for attribute '" + attributeName + "' of " + ownerEntityType.getJavaType().getName() + " found which is not yet supported. Consider using the primary key instead!");
                    }
                }
                attributeOwnerFlusher = ownerIdFlusher;
            } else if (ownerIdFlusher.getMapping().equals(joinTableOwnerProperties.values().iterator().next())) {
                attributeOwnerFlusher = ownerIdFlusher;
            } else {
                attributeOwnerFlusher = findSingularAttributeFlusherByMapping(evm, owner, viewType, attributeName, joinTableOwnerProperties.keySet().iterator().next());
            }
        }
        TypeDescriptor elementDescriptor = TypeDescriptor.forType(evm, this, attribute, pluralAttribute.getElementType(), owner, ownerMapping);
        boolean collectionUpdatable = attribute.isUpdatable();
        CollectionRemoveListener elementRemoveListener = createOrphanRemoveListener(attribute, elementDescriptor);
        CollectionRemoveListener elementCascadeDeleteListener = createCascadeDeleteListener(attribute, elementDescriptor);
        boolean jpaProviderDeletesCollection;
        boolean supportsCollectionDml = jpaProvider.supportsInsertStatement();

        if (elementDescriptor.getAttributeIdAttributeName() != null) {
            jpaProviderDeletesCollection = jpaProvider.supportsJoinTableCleanupOnDelete();
        } else {
            jpaProviderDeletesCollection = jpaProvider.supportsCollectionTableCleanupOnDelete();
        }

        if (attribute instanceof MapAttribute<?, ?, ?>) {
            MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) attribute;
            TypeDescriptor keyDescriptor = TypeDescriptor.forType(evm, this, attribute, mapAttribute.getKeyType(), owner, ownerMapping);
            // TODO: currently there is no possibility to set this
            CollectionRemoveListener keyRemoveListener = null;
            CollectionRemoveListener keyCascadeDeleteListener = null;

            if (collectionUpdatable || keyDescriptor.shouldFlushMutations() || elementDescriptor.shouldFlushMutations() || shouldPassThrough(evm, viewType, attribute)) {
                MapViewToEntityMapper mapper = new SimpleMapViewToEntityMapper(keyDescriptor.getViewToEntityMapper(), elementDescriptor.getViewToEntityMapper());
                MapViewToEntityMapper loadOnlyMapper = new SimpleMapViewToEntityMapper(keyDescriptor.getLoadOnlyViewToEntityMapper(), elementDescriptor.getLoadOnlyViewToEntityMapper());

                MapInstantiator<?, ?> mapInstantiator = attribute.getMapInstantiator();
                return new MapAttributeFlusher<Object, RecordingMap<Map<?, ?>, ?, ?>>(
                        attributeName,
                        attributeMapping,
                        owner == null ? entityClass : owner.fullEntityLoader.getEntityClass(),
                        idAttributeName,
                        ownerMapping,
                        attributeOwnerFlusher,
                        createPluralAttributeSubFlusher(evm, viewType, attribute, "element", mapAttribute.getElementType(), owner, ownerMapping),
                        supportsCollectionDml,
                        flushStrategy,
                        entityAttributeAccessor,
                        viewAttributeAccessor,
                        optimisticLockProtected,
                        collectionUpdatable,
                        keyCascadeDeleteListener,
                        elementCascadeDeleteListener,
                        keyRemoveListener,
                        elementRemoveListener,
                        viewOnlyDeleteCascaded,
                        jpaProviderDeletesCollection,
                        keyDescriptor,
                        elementDescriptor,
                        mapper,
                        loadOnlyMapper,
                        mapInstantiator
                );
            } else {
                return null;
            }
        } else {
            if (collectionUpdatable || elementDescriptor.shouldFlushMutations() || shouldPassThrough(evm, viewType, attribute)) {
                InverseFlusher<Object> inverseFlusher = InverseFlusher.forAttribute(evm, viewType, attribute, elementDescriptor, owner, ownerMapping);
                InverseRemoveStrategy inverseRemoveStrategy = attribute.getInverseRemoveStrategy();

                CollectionInstantiator collectionInstantiator = attribute.getCollectionInstantiator();
                if (pluralAttribute.isIndexed()) {
                    return new IndexedListAttributeFlusher<Object, RecordingList<List<?>>>(
                            attributeName,
                            attributeMapping,
                            owner == null ? entityClass : owner.fullEntityLoader.getEntityClass(),
                            idAttributeName,
                            ownerMapping,
                            attributeOwnerFlusher,
                            createPluralAttributeSubFlusher(evm, viewType, attribute, "element", pluralAttribute.getElementType(), owner, ownerMapping),
                            supportsCollectionDml,
                            flushStrategy,
                            entityAttributeAccessor,
                            viewAttributeAccessor,
                            optimisticLockProtected,
                            collectionUpdatable,
                            viewOnlyDeleteCascaded,
                            jpaProviderDeletesCollection,
                            elementCascadeDeleteListener,
                            elementRemoveListener,
                            collectionInstantiator,
                            elementDescriptor,
                            inverseFlusher,
                            inverseRemoveStrategy
                    );
                } else {
                    return new CollectionAttributeFlusher(
                            attributeName,
                            attributeMapping,
                            owner == null ? entityClass : owner.fullEntityLoader.getEntityClass(),
                            idAttributeName,
                            ownerMapping,
                            attributeOwnerFlusher,
                            createPluralAttributeSubFlusher(evm, viewType, attribute, "element", pluralAttribute.getElementType(), owner, ownerMapping),
                            supportsCollectionDml,
                            flushStrategy,
                            entityAttributeAccessor,
                            viewAttributeAccessor,
                            optimisticLockProtected,
                            collectionUpdatable,
                            viewOnlyDeleteCascaded,
                            jpaProviderDeletesCollection,
                            elementCascadeDeleteListener,
                            elementRemoveListener,
                            collectionInstantiator,
                            elementDescriptor,
                            inverseFlusher,
                            inverseRemoveStrategy
                    );
                }
            } else {
                return null;
            }
        }
    }

    private DirtyAttributeFlusher<?, ?, ?> findSingularAttributeFlusherByMapping(EntityViewManagerImpl evm, EntityViewUpdaterImpl owner, ManagedViewTypeImplementor<?> viewType, String attributeName, String mapping) {
        ManagedViewTypeImplementor<?> managedViewTypeImplementor = owner == null ? viewType : owner.managedViewType;
        for (MethodAttribute<?, ?> attribute : managedViewTypeImplementor.getAttributes()) {
            if (attribute instanceof com.blazebit.persistence.view.metamodel.SingularAttribute<?, ?> && mapping.equals(((MappingAttribute<?, ?>) attribute).getMapping())) {
                return createIdFlusher(evm, (ViewType<?>) managedViewTypeImplementor, createViewIdMapper(evm, (com.blazebit.persistence.view.metamodel.SingularAttribute<?, ?>) attribute), (AbstractMethodAttribute<?, ?>) attribute);
            }
        }

        // TODO: we could implement loading by id, but that's gonna take some more work
        throw new IllegalArgumentException("Can't create plural attribute flusher for entity view attribute " + attributeName + " because owner entity view " + managedViewTypeImplementor.getJavaType().getName() +
                " is missing an attribute for the join key attributes: " + mapping);
    }

    private DirtyAttributeFlusher<?, ?, ?> createPluralAttributeSubFlusher(EntityViewManagerImpl evm, ManagedViewTypeImplementor<?> viewType, AbstractMethodAttribute<?, ?> attribute, String name, Type<?> type, EntityViewUpdaterImpl owner, String ownerMapping) {
        EntityMetamodel entityMetamodel = evm.getMetamodel().getEntityMetamodel();
        String attributeName = attribute.getName() + "_" + name;
        String attributeMapping = attribute.getMapping();
        String attributeLocation = attribute.getLocation();
        Set<Type<?>> readOnlyAllowedSubtypes = attribute.getReadOnlyAllowedSubtypes();
        Set<Type<?>> persistAllowedSubtypes = attribute.getPersistCascadeAllowedSubtypes();
        Set<Type<?>> updateAllowedSubtypes = attribute.getUpdateCascadeAllowedSubtypes();
        if (type instanceof ManagedViewType<?>) {
            ManagedViewType<?> subviewType = (ManagedViewType<?>) type;
            if (entityMetamodel.getEntity(subviewType.getEntityClass()) == null) {
                // A singular attribute where the subview refers to an embeddable type
                EmbeddableUpdaterBasedViewToEntityMapper viewToEntityMapper = new EmbeddableUpdaterBasedViewToEntityMapper(
                        attributeLocation,
                        evm,
                        subviewType.getJavaType(),
                        readOnlyAllowedSubtypes,
                        persistAllowedSubtypes,
                        updateAllowedSubtypes,
                        ReferenceEntityLoader.forAttribute(evm, subviewType, attribute),
                        false,
                        null,
                        owner,
                        ownerMapping == null ? attributeMapping : ownerMapping + "." + attributeMapping
                );

                String parameterName = attributeName + "_";
                String updateFragment = attributeMapping + ".";

                return new EmbeddableAttributeFlusher<>(
                        attributeName,
                        attributeMapping,
                        updateFragment,
                        parameterName,
                        false,
                        false,
                        false,
                        null,
                        null,
                        viewToEntityMapper
                );
            } else {
                ViewTypeImplementor<?> attributeViewType = (ViewTypeImplementor<?>) subviewType;
                InitialValueAttributeAccessor viewAttributeAccessor = Accessors.forMutableViewAttribute(evm, attribute);
                AttributeAccessor subviewIdAccessor = Accessors.forViewId(evm, attributeViewType, true);

                // TODO: Shouldn't we use the "attribute element id" mapping?
                String idMapping = ((MappingAttribute) attributeViewType.getIdAttribute()).getMapping();
                Attribute<?, ?> attributeIdAttribute = attributeViewType.getJpaManagedType().getAttribute(idMapping);
                javax.persistence.metamodel.Type<?> attributeIdAttributeType = entityMetamodel.type(JpaMetamodelUtils.resolveFieldClass(attributeViewType.getEntityClass(), attributeIdAttribute));
                List<String> idComponentMappings;
                boolean requiresComponentWiseSetInUpdate = true;

                if (requiresComponentWiseSetInUpdate && attributeIdAttributeType instanceof EmbeddableType<?>) {
                    // If the identifier used for the association is an embeddable, we must collect the individual attribute components since updates don't work on embeddables directly
                    Set<Attribute<?, ?>> idAttributeComponents = (Set<Attribute<?, ?>>) ((EmbeddableType<?>) attributeIdAttributeType).getAttributes();
                    idComponentMappings = new ArrayList<>(idAttributeComponents.size());
                    for (Attribute<?, ?> idAttributeComponent : idAttributeComponents) {
                        idComponentMappings.add(attributeMapping + "." + idMapping + "." + idAttributeComponent);
                    }
                } else {
                    idComponentMappings = Collections.singletonList(attributeMapping + "." + idMapping);
                }

                String[] idAttributeMappings = idComponentMappings.toArray(new String[idComponentMappings.size()]);

                ManagedType<?> ownerManagedType = owner == null ? viewType.getJpaManagedType() : owner.managedViewType.getJpaManagedType();
                EntityType<?> ownerEntityType = ownerManagedType instanceof EntityType<?> ? (EntityType<?>) ownerManagedType : null;
                ViewToEntityMapper viewToEntityMapper = createViewToEntityMapper(attributeLocation, evm, ownerEntityType, attributeName, attributeMapping, attributeViewType, false, false, readOnlyAllowedSubtypes, persistAllowedSubtypes, updateAllowedSubtypes, attribute.getViewTypes(), owner, ownerMapping);
                String parameterName = attributeName;

                return new SubviewAttributeFlusher<>(
                        attributeName,
                        attributeMapping,
                        false,
                        true,
                        false,
                        false,
                        false,
                        subviewType.getConverter(),
                        false,
                        idAttributeMappings,
                        parameterName,
                        false,
                        owner != null,
                        null,
                        viewAttributeAccessor,
                        subviewIdAccessor,
                        viewToEntityMapper,
                        null,
                        null
                );
            }
        } else {
            TypeDescriptor elementDescriptor = TypeDescriptor.forType(evm, this, attribute, type, owner, ownerMapping);
            String parameterName = attributeName;
            String updateFragment = attributeMapping;

            // TODO: Why?
            boolean supportsQueryFlush = !elementDescriptor.isJpaEmbeddable();
            return new BasicAttributeFlusher<>(
                    attributeName,
                    attributeMapping,
                    supportsQueryFlush,
                    false,
                    true,
                    false,
                    false,
                    false,
                    null,
                    elementDescriptor,
                    updateFragment,
                    parameterName,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
    }

    private DirtyAttributeFlusher<?, ?, ?> createSingularAttributeFlusher(EntityViewManagerImpl evm, ManagedViewTypeImplementor<?> viewType, AbstractMethodAttribute<?, ?> attribute, EntityViewUpdaterImpl owner, String ownerMapping) {
        EntityMetamodel entityMetamodel = evm.getMetamodel().getEntityMetamodel();
        Class<?> entityClass = viewType.getEntityClass();
        String attributeName = attribute.getName();
        String attributeMapping = attribute.getMapping();
        AttributeAccessor entityAttributeAccessor = Accessors.forEntityMapping(evm, attribute);
        String attributeLocation = attribute.getLocation();
        boolean cascadePersist = attribute.isPersistCascaded();
        boolean cascadeUpdate = attribute.isUpdateCascaded();
        boolean cascadeDelete = attribute.isDeleteCascaded();
        boolean viewOnlyDeleteCascaded = cascadeDelete && !entityMetamodel.getManagedType(ExtendedManagedType.class, entityClass).getAttribute(attributeMapping).isDeleteCascaded();
        boolean optimisticLockProtected = attribute.isOptimisticLockProtected();
        Set<Type<?>> readOnlyAllowedSubtypes = attribute.getReadOnlyAllowedSubtypes();
        Set<Type<?>> persistAllowedSubtypes = attribute.getPersistCascadeAllowedSubtypes();
        Set<Type<?>> updateAllowedSubtypes = attribute.getUpdateCascadeAllowedSubtypes();
        JpaProvider jpaProvider = evm.getJpaProvider();
        if (attribute.isSubview()) {
            boolean shouldFlushUpdates = cascadeUpdate && !updateAllowedSubtypes.isEmpty();
            boolean shouldFlushPersists = cascadePersist && !persistAllowedSubtypes.isEmpty();

            ManagedViewTypeImplementor<?> subviewType = (ManagedViewTypeImplementor<?>) ((com.blazebit.persistence.view.metamodel.SingularAttribute<?, ?>) attribute).getType();
            boolean passThrough = false;

            if (attribute.isUpdatable() || shouldFlushUpdates || (passThrough = shouldPassThrough(evm, viewType, attribute))) {
                // TODO: shouldn't this be done for any flat view? or are updatable flat views for entity types disallowed?
                if (entityMetamodel.getEntity(subviewType.getEntityClass()) == null) {
                    AttributeAccessor viewAttributeAccessor = Accessors.forViewAttribute(evm, attribute, true);
                    // A singular attribute where the subview refers to an embeddable type
                    EmbeddableUpdaterBasedViewToEntityMapper viewToEntityMapper = new EmbeddableUpdaterBasedViewToEntityMapper(
                            attributeLocation,
                            evm,
                            subviewType.getJavaType(),
                            readOnlyAllowedSubtypes,
                            persistAllowedSubtypes,
                            updateAllowedSubtypes,
                            ReferenceEntityLoader.forAttribute(evm, subviewType, attribute),
                            shouldFlushPersists,
                            null,
                            owner == null ? this : owner,
                            ownerMapping == null ? attributeMapping : ownerMapping + "." + attributeMapping
                    );

                    CompositeAttributeFlusher nestedFlusher = (CompositeAttributeFlusher) viewToEntityMapper.getFullGraphNode();
                    boolean supportsQueryFlush = nestedFlusher.supportsQueryFlush() && jpaProvider.supportsUpdateSetEmbeddable();
                    String parameterName;
                    String updateFragment;
                    if (supportsQueryFlush) {
                        parameterName = attributeName;
                        updateFragment = attributeMapping;
                    } else {
                        parameterName = attributeName + "_";
                        updateFragment = attributeMapping + ".";
                    }

                    return new EmbeddableAttributeFlusher<>(
                            attributeName,
                            attributeMapping,
                            updateFragment,
                            parameterName,
                            optimisticLockProtected,
                            passThrough,
                            supportsQueryFlush,
                            entityAttributeAccessor,
                            viewAttributeAccessor,
                            viewToEntityMapper
                    );
                } else {
                    // Subview refers to entity type
                    ViewTypeImplementor<?> attributeViewType = (ViewTypeImplementor<?>) subviewType;
                    InitialValueAttributeAccessor viewAttributeAccessor = Accessors.forMutableViewAttribute(evm, attribute);
                    AttributeAccessor subviewIdAccessor = Accessors.forViewId(evm, attributeViewType, true);
                    InverseFlusher<Object> inverseFlusher = InverseFlusher.forAttribute(evm, viewType, attribute, TypeDescriptor.forType(evm, this, attribute, subviewType, owner, ownerMapping), owner, ownerMapping);
                    InverseRemoveStrategy inverseRemoveStrategy = attribute.getInverseRemoveStrategy();
                    ViewToEntityMapper viewToEntityMapper;
                    boolean fetch = shouldFlushUpdates;
                    String parameterName = null;

                    // TODO: again, shouldn't we use the "attribute element id" mapping?
                    String idMapping = ((MappingAttribute) attributeViewType.getIdAttribute()).getMapping();
                    Attribute<?, ?> attributeIdAttribute = attributeViewType.getJpaManagedType().getAttribute(idMapping);
                    javax.persistence.metamodel.Type<?> attributeIdAttributeType = entityMetamodel.type(JpaMetamodelUtils.resolveFieldClass(attributeViewType.getEntityClass(), attributeIdAttribute));
                    List<String> idComponentMappings;
                    boolean requiresComponentWiseSetInUpdate = true;

                    if (requiresComponentWiseSetInUpdate && attributeIdAttributeType instanceof EmbeddableType<?>) {
                        // If the identifier used for the association is an embeddable, we must collect the individual attribute components since updates don't work on embeddables directly
                        Set<Attribute<?, ?>> idAttributeComponents = (Set) ((EmbeddableType<?>) attributeIdAttributeType)
                                .getAttributes();
                        idComponentMappings = new ArrayList<>(idAttributeComponents.size());
                        for (Attribute<?, ?> idAttributeComponent : idAttributeComponents) {
                            idComponentMappings.add(attributeMapping + "." + idMapping + "." + idAttributeComponent.getName());
                        }
                    } else {
                        idComponentMappings = Collections.singletonList(attributeMapping + "." + idMapping);
                    }

                    String[] idAttributeMappings = idComponentMappings.toArray(new String[idComponentMappings.size()]);

                    ManagedType<?> ownerEntityType = owner == null ? viewType.getJpaManagedType() : owner.managedViewType.getJpaManagedType();
                    if (attribute.isUpdatable() && ownerEntityType instanceof EntityType<?>) {
                        viewToEntityMapper = createViewToEntityMapper(attributeLocation, evm, (EntityType<?>) ownerEntityType, attributeName, attributeMapping, attributeViewType, cascadePersist, cascadeUpdate, readOnlyAllowedSubtypes, persistAllowedSubtypes, updateAllowedSubtypes, attribute.getViewTypes(), owner, ownerMapping);
                        parameterName = attributeName;
                    } else {
                        String elementIdentifier;
                        if (ownerEntityType instanceof EntityType<?>) {
                            elementIdentifier = TypeDescriptor.getAttributeElementIdentifier(evm, (EntityType<?>) ownerEntityType, attributeName, ownerMapping, attributeMapping, attributeViewType.getJpaManagedType());
                        } else {
                            elementIdentifier = null;
                        }
                        AttributeAccessor entityIdAccessor = Accessors.forEntityMapping(evm, attributeViewType.getEntityClass(), elementIdentifier);
                        if (shouldFlushUpdates) {
                            viewToEntityMapper = new UpdaterBasedViewToEntityMapper(
                                    attributeLocation,
                                    evm,
                                    subviewType.getJavaType(),
                                    readOnlyAllowedSubtypes,
                                    persistAllowedSubtypes,
                                    updateAllowedSubtypes,
                                    ReferenceEntityLoader.forAttribute(evm, subviewType, attribute),
                                    Accessors.forViewId(evm, attributeViewType, true),
                                    entityIdAccessor,
                                    shouldFlushPersists,
                                    owner,
                                    ownerMapping
                            );
                        } else if (shouldFlushPersists) {
                            viewToEntityMapper = new CreateOnlyViewToEntityMapper(
                                    attributeLocation,
                                    evm,
                                    subviewType.getJavaType(),
                                    readOnlyAllowedSubtypes,
                                    persistAllowedSubtypes,
                                    updateAllowedSubtypes,
                                    null,
                                    null,
                                    entityIdAccessor,
                                    shouldFlushPersists,
                                    owner,
                                    ownerMapping
                            );
                        } else {
                            viewToEntityMapper = new LoadOrPersistViewToEntityMapper(
                                    attributeLocation,
                                    evm,
                                    subviewType.getJavaType(),
                                    readOnlyAllowedSubtypes,
                                    persistAllowedSubtypes,
                                    updateAllowedSubtypes,
                                    ReferenceEntityLoader.forAttribute(evm, subviewType, attribute),
                                    null,
                                    entityIdAccessor,
                                    shouldFlushPersists,
                                    owner,
                                    ownerMapping
                            );
                        }
                    }

                    return new SubviewAttributeFlusher<>(
                            attributeName,
                            attributeMapping,
                            optimisticLockProtected,
                            attribute.isUpdatable(),
                            cascadeDelete,
                            attribute.isOrphanRemoval(),
                            viewOnlyDeleteCascaded,
                            subviewType.getConverter(),
                            fetch,
                            idAttributeMappings,
                            parameterName,
                            passThrough,
                            owner != null,
                            entityAttributeAccessor,
                            viewAttributeAccessor,
                            subviewIdAccessor,
                            viewToEntityMapper,
                            inverseFlusher,
                            inverseRemoveStrategy
                    );
                }
            } else {
                return null;
            }
        } else {
            BasicTypeImpl<?> attributeType = (BasicTypeImpl<?>) ((com.blazebit.persistence.view.metamodel.SingularAttribute<?, ?>) attribute).getType();
            TypeDescriptor elementDescriptor = TypeDescriptor.forType(evm, this, attribute, attributeType, owner, ownerMapping);
            // Basic attributes like String, Integer but also JPA managed types
            boolean updatable = attribute.isUpdatable();

            if (updatable || elementDescriptor.shouldFlushMutations() || shouldPassThrough(evm, viewType, attribute)) {
                // Basic attributes can normally be updated by queries
                InverseFlusher<Object> inverseFlusher = InverseFlusher.forAttribute(evm, viewType, attribute, elementDescriptor, owner, ownerMapping);
                InverseRemoveStrategy inverseRemoveStrategy = attribute.getInverseRemoveStrategy();
                String parameterName = attributeName;
                String updateFragment = attributeMapping;
                ManagedType<?> ownerEntityType = owner == null ? viewType.getJpaManagedType() : owner.managedViewType.getJpaManagedType();
                UnmappedBasicAttributeCascadeDeleter deleter;

                if (elementDescriptor.isJpaEntity() && cascadeDelete && ownerEntityType instanceof EntityType<?>) {
                    String elementIdAttributeName = TypeDescriptor.getAttributeElementIdentifier(evm, (EntityType<?>) ownerEntityType, attributeName, ownerMapping, attributeMapping, attributeType.getManagedType());
                    deleter = new UnmappedBasicAttributeCascadeDeleter(
                            evm,
                            attributeName,
                            entityMetamodel.getManagedType(ExtendedManagedType.class, entityClass).getAttribute(attributeMapping),
                            attributeMapping + "." + elementIdAttributeName,
                            false
                    );
                } else {
                    deleter = null;
                }

                // When wanting to read the actual value of non-updatable attributes or writing values to attributes we need the view attribute accessor
                // Whenever we merge or persist, we are going to need that
                AttributeAccessor viewAttributeAccessor;
                if (elementDescriptor.shouldFlushMutations()) {
                    viewAttributeAccessor = Accessors.forMutableViewAttribute(evm, attribute);
                } else {
                    viewAttributeAccessor = Accessors.forViewAttribute(evm, attribute, true);
                }

                Map.Entry<AttributeAccessor, BasicAttributeFlusher>[] componentFlusherEntries = null;
                if (elementDescriptor.isJpaEmbeddable()) {
                    if (!jpaProvider.supportsUpdateSetEmbeddable()) {
                        Set<Attribute<?, ?>> attributes = (Set) attributeType.getManagedType().getAttributes();
                        Map<AttributeAccessor, BasicAttributeFlusher> componentFlushers = new HashMap<>(attributes.size());
                        buildComponentFlushers(evm, viewType.getEntityClass(), attributeType.getJavaType(), attributeName + "_", attributeMapping + ".", "", attributes, componentFlushers);
                        componentFlusherEntries = componentFlushers.entrySet().toArray(new Map.Entry[componentFlushers.size()]);
                    }
                }

                return new BasicAttributeFlusher<>(
                        attributeName,
                        attributeMapping,
                        true,
                        optimisticLockProtected,
                        updatable,
                        cascadeDelete,
                        attribute.isOrphanRemoval(),
                        viewOnlyDeleteCascaded,
                        componentFlusherEntries,
                        elementDescriptor,
                        updateFragment,
                        parameterName,
                        entityAttributeAccessor,
                        viewAttributeAccessor,
                        deleter,
                        inverseFlusher,
                        inverseRemoveStrategy
                );
            } else {
                return null;
            }
        }
    }

    private static CollectionRemoveListener createOrphanRemoveListener(AbstractMethodAttribute<?, ?> attribute, TypeDescriptor elementDescriptor) {
        if (!attribute.isOrphanRemoval()) {
            return null;
        }

        if (elementDescriptor.isSubview()) {
            return new ViewCollectionRemoveListener(elementDescriptor.getLoadOnlyViewToEntityMapper());
        } else {
            return EntityCollectionRemoveListener.INSTANCE;
        }
    }

    private static CollectionRemoveListener createCascadeDeleteListener(AbstractMethodAttribute<?, ?> attribute, TypeDescriptor elementDescriptor) {
        if (!attribute.isDeleteCascaded()) {
            return null;
        }

        if (elementDescriptor.isSubview()) {
            return new ViewCollectionRemoveListener(elementDescriptor.getLoadOnlyViewToEntityMapper());
        } else {
            return EntityCollectionRemoveListener.INSTANCE;
        }
    }

    private static boolean shouldPassThrough(EntityViewManagerImpl evm, ManagedViewType<?> viewType, AbstractMethodAttribute<?, ?> attribute) {
        // For an attribute being eligible for pass through, the declaring view must be for an embeddable type
        // and the attribute must be update mappable
        return evm.getMetamodel().getEntityMetamodel().getEntity(viewType.getEntityClass()) == null
                && attribute.isUpdateMappable();
    }

    private static ViewToEntityMapper createViewToEntityMapper(String attributeLocation, EntityViewManagerImpl evm, EntityType<?> ownerEntityType, String attributeName, String attributeMapping, ViewTypeImplementor<?> viewType, boolean cascadePersist, boolean cascadeUpdate,
                                                               Set<Type<?>> readOnlyAllowedSubtypes, Set<Type<?>> persistAllowedSubtypes, Set<Type<?>> updateAllowedSubtypes, Set<ManagedViewType<?>> viewTypes, EntityViewUpdaterImpl owner, String ownerMapping) {
        EntityLoader entityLoader = ReferenceEntityLoader.forAttribute(evm, viewType, viewTypes);
        AttributeAccessor viewIdAccessor = Accessors.forViewId(evm, viewType, true);
        String elementIdentifier;
        if (ownerEntityType == null) {
            elementIdentifier = null;
        } else {
            elementIdentifier = TypeDescriptor.getAttributeElementIdentifier(evm, ownerEntityType, attributeName, ownerMapping, attributeMapping, viewType.getJpaManagedType());
        }
        AttributeAccessor entityIdAccessor = Accessors.forEntityMapping(evm, viewType.getEntityClass(), elementIdentifier);

        Class<?> viewTypeClass = viewType.getJavaType();
        boolean mutable = viewType.isUpdatable() || viewType.isCreatable() || !persistAllowedSubtypes.isEmpty() || !updateAllowedSubtypes.isEmpty();
        if (!mutable || !cascadeUpdate) {
            return new LoadOrPersistViewToEntityMapper(
                    attributeLocation,
                    evm,
                    viewTypeClass,
                    readOnlyAllowedSubtypes,
                    persistAllowedSubtypes,
                    updateAllowedSubtypes,
                    entityLoader,
                    viewIdAccessor,
                    entityIdAccessor,
                    cascadePersist,
                    owner,
                    ownerMapping
            );
        }

        return new UpdaterBasedViewToEntityMapper(
                attributeLocation,
                evm,
                viewTypeClass,
                readOnlyAllowedSubtypes,
                persistAllowedSubtypes,
                updateAllowedSubtypes,
                entityLoader,
                viewIdAccessor,
                entityIdAccessor,
                cascadePersist,
                owner,
                ownerMapping
        );
    }

}
