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

package com.blazebit.persistence.view.impl.update;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.impl.EntityMetamodel;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.util.JpaMetamodelUtils;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.OptimisticLockException;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.accessor.Accessors;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.accessor.InitialValueAttributeAccessor;
import com.blazebit.persistence.view.impl.change.DirtyChecker;
import com.blazebit.persistence.view.impl.collection.CollectionInstantiator;
import com.blazebit.persistence.view.impl.collection.MapInstantiator;
import com.blazebit.persistence.view.impl.collection.RecordingList;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.impl.entity.CreateOnlyViewToEntityMapper;
import com.blazebit.persistence.view.impl.entity.EmbeddableUpdaterBasedViewToEntityMapper;
import com.blazebit.persistence.view.impl.entity.EntityLoader;
import com.blazebit.persistence.view.impl.entity.EntityTupleizer;
import com.blazebit.persistence.view.impl.entity.FullEntityLoader;
import com.blazebit.persistence.view.impl.entity.LoadOrPersistViewToEntityMapper;
import com.blazebit.persistence.view.impl.entity.MapViewToEntityMapper;
import com.blazebit.persistence.view.impl.entity.ReferenceEntityLoader;
import com.blazebit.persistence.view.impl.entity.UpdaterBasedViewToEntityMapper;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.ViewTypeImpl;
import com.blazebit.persistence.view.impl.proxy.DirtyStateTrackable;
import com.blazebit.persistence.view.impl.proxy.MutableStateTrackable;
import com.blazebit.persistence.view.impl.update.flush.BasicAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.CollectionAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.CompositeAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.DirtyAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.EmbeddableAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.FetchGraphNode;
import com.blazebit.persistence.view.impl.update.flush.IndexedListAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.InverseFlusher;
import com.blazebit.persistence.view.impl.update.flush.MapAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.SimpleMapViewToEntityMapper;
import com.blazebit.persistence.view.impl.update.flush.SubviewAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.TypeDescriptor;
import com.blazebit.persistence.view.impl.update.flush.VersionAttributeFlusher;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.BasicType;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;
import com.blazebit.persistence.view.spi.type.VersionBasicUserType;

import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityViewUpdaterImpl implements EntityViewUpdater {

    public static final String ID_PARAM_NAME = "_id";
    public static final String VERSION_PARAM_NAME = "_version";

    private final boolean fullFlush;
    private final boolean rootUpdateAllowed;
    private final FlushStrategy flushStrategy;
    private final EntityLoader fullEntityLoader;
    private final DirtyAttributeFlusher<?, Object, Object> idFlusher;
    private final CompositeAttributeFlusher fullFlusher;
    private final String updatePrefixString;
    private final String updatePostfixString;
    private final String fullUpdateQueryString;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public EntityViewUpdaterImpl(EntityViewManagerImpl evm, ManagedViewTypeImplementor<?> viewType) {
        Class<?> entityClass = viewType.getEntityClass();
        this.fullFlush = viewType.getFlushMode() == FlushMode.FULL;
        this.flushStrategy = viewType.getFlushStrategy();
        EntityType<?> entityType = evm.getMetamodel().getEntityMetamodel().getEntity(entityClass);
        ViewToEntityMapper viewIdMapper = null;

        final AttributeAccessor viewIdAccessor;
        final EntityTupleizer tupleizer;
        final ObjectBuilder<Object> idViewBuilder;
        boolean persistable = entityType != null;
        if (persistable && viewType instanceof ViewType<?>) {
            // To be able to invoke EntityViewManager#update on an updatable view of this type, it must have an id i.e. be a ViewType instead of a FlatViewType
            // Otherwise we can't load the object itself
            ViewType<?> view = (ViewType<?>) viewType;
            this.rootUpdateAllowed = true;
            viewIdAccessor = Accessors.forViewId(evm, (ViewType<?>) viewType, false);
            if (view.getIdAttribute().isSubview()) {
                com.blazebit.persistence.view.metamodel.SingularAttribute<?, ?> viewIdAttribute =
                        (com.blazebit.persistence.view.metamodel.SingularAttribute<?, ?>) view.getIdAttribute();
                ManagedViewTypeImplementor<?> viewIdType = (ManagedViewTypeImplementor<?>) viewIdAttribute.getType();

                viewIdMapper = createViewIdMapper(evm, view);
                tupleizer = new DefaultEntityTupleizer(evm, viewIdType);
                ExpressionFactory ef = evm.getCriteriaBuilderFactory().getService(ExpressionFactory.class);
                idViewBuilder = (ObjectBuilder<Object>) evm.getTemplate(
                        ef,
                        viewIdType,
                        null,
                        viewIdType.getJavaType().getSimpleName(),
                        null,
                        0
                ).createObjectBuilder(null, null, null);
            } else {
                tupleizer = null;
                idViewBuilder = null;
            }
            this.idFlusher = createIdFlusher(evm, view, viewIdMapper);
        } else {
            this.rootUpdateAllowed = false;
            viewIdAccessor = null;
            tupleizer = null;
            idViewBuilder = null;
            this.idFlusher = null;
        }
        this.fullEntityLoader = new FullEntityLoader(evm, viewType);
        Set<MethodAttribute<?, ?>> attributes = (Set<MethodAttribute<?, ?>>) (Set<?>) viewType.getAttributes();
        AbstractMethodAttribute<?, ?> idAttribute;
        AbstractMethodAttribute<?, ?> versionAttribute;
        DirtyAttributeFlusher<?, Object, Object> versionFlusher;

        if (viewType instanceof ViewType<?>) {
            idAttribute = (AbstractMethodAttribute<?, ?>) ((ViewType) viewType).getIdAttribute();
            versionAttribute = (AbstractMethodAttribute<?, ?>) ((ViewType) viewType).getVersionAttribute();
            if (versionAttribute != null) {
                versionFlusher = createVersionFlusher(evm, entityType, versionAttribute);
            } else {
                versionFlusher = null;
            }
        } else {
            idAttribute = null;
            versionAttribute = null;
            versionFlusher = null;
        }

        javax.persistence.metamodel.SingularAttribute<?, ?> jpaIdAttribute = null;

        if (idAttribute != null) {
            jpaIdAttribute = JpaMetamodelUtils.getIdAttribute(evm.getMetamodel().getEntityMetamodel().entity(entityClass));
            String mapping = idAttribute.getMapping();
            // Read only entity views don't have this restriction
            if ((viewType.isCreatable() || viewType.isUpdatable()) && !mapping.equals(jpaIdAttribute.getName())) {
                throw new IllegalArgumentException("Expected JPA id attribute [" + jpaIdAttribute.getName() + "] to match the entity view id attribute mapping [" + mapping + "] but it didn't!");
            }
        }

        // Flushers are ordered like the dirty and initial state array and have matching indexes
        // Since attributes for which pass through flushers are created are not updatable and not mutable, they must come last, as there is no dirty state index for them
        List<DirtyAttributeFlusher<?, ?, ?>> flushers = new ArrayList<>(attributes.size());
        List<DirtyAttributeFlusher<?, ?, ?>> passThroughFlushers = null;

        StringBuilder sb = null;
        int clauseEndIndex = -1;

        if (flushStrategy != FlushStrategy.ENTITY && jpaIdAttribute != null) {
            this.updatePrefixString = "UPDATE " + entityType.getName() + " e SET ";
            if (versionAttribute != null) {
                this.updatePostfixString = " WHERE e." + jpaIdAttribute.getName() + " = :" + ID_PARAM_NAME +
                        " AND e." + versionAttribute.getMapping() + " = :" + VERSION_PARAM_NAME;
            } else {
                this.updatePostfixString = " WHERE e." + jpaIdAttribute.getName() + " = :" + ID_PARAM_NAME;
            }
            sb = new StringBuilder(updatePrefixString.length() + updatePostfixString.length() + attributes.size() * 50);
            sb.append(updatePrefixString);
            clauseEndIndex = sb.length();
        } else {
            this.updatePrefixString = null;
            this.updatePostfixString = null;
        }

        if (versionFlusher != null && sb != null) {
            versionFlusher.appendUpdateQueryFragment(null, sb, null, null);
            // If something was appended, we also append a comma
            if (clauseEndIndex != sb.length()) {
                clauseEndIndex = sb.length();
                sb.append(", ");
            }
        }

        // Only construct attribute flushers for creatable or updatable entity views
        if (viewType.isCreatable() || viewType.isUpdatable()) {
            for (MethodAttribute<?, ?> attribute : attributes) {
                if (attribute == idAttribute || attribute == versionAttribute) {
                    continue;
                }
                AbstractMethodAttribute<?, ?> methodAttribute = (AbstractMethodAttribute<?, ?>) attribute;
                // Skip attributes that aren't update mappable
                if (!methodAttribute.isUpdateMappable()) {
                    continue;
                }
                DirtyAttributeFlusher flusher = createAttributeFlusher(evm, viewType, flushStrategy, methodAttribute);
                if (flusher != null) {
                    if (sb != null) {
                        int endIndex = sb.length();
                        flusher.appendUpdateQueryFragment(null, sb, null, null);

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

        this.fullFlusher = new CompositeAttributeFlusher(
                viewType.getJavaType(),
                entityClass,
                persistable,
                jpaIdAttribute,
                viewIdMapper,
                viewIdAccessor,
                tupleizer,
                idViewBuilder,
                idFlusher,
                versionFlusher,
                flushers.toArray(new DirtyAttributeFlusher[flushers.size()]),
                viewType.getFlushMode(),
                flushStrategy
        );
        if (flushStrategy != FlushStrategy.ENTITY && jpaIdAttribute != null && clauseEndIndex != sb.length()) {
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

    public static ViewToEntityMapper createViewIdMapper(EntityViewManagerImpl evm, ManagedViewType<?> viewType) {
        ViewType<?> view;
        if (!(viewType instanceof ViewType<?>) || !(view = (ViewType<?>) viewType).getIdAttribute().isSubview()) {
            return null;
        }

        com.blazebit.persistence.view.metamodel.SingularAttribute<?, ?> viewIdAttribute =
                (com.blazebit.persistence.view.metamodel.SingularAttribute<?, ?>) view.getIdAttribute();
        ManagedViewTypeImplementor<?> viewIdType = (ManagedViewTypeImplementor<?>) viewIdAttribute.getType();

        return new EmbeddableUpdaterBasedViewToEntityMapper(
                ((AbstractMethodAttribute<?, ?>) view.getIdAttribute()).getLocation(),
                evm,
                viewIdType.getJavaType(),
                Collections.<Type<?>>singleton(viewIdType),
                Collections.<Type<?>>emptySet(),
                new ReferenceEntityLoader(evm, viewIdType, null),
                true
        );
    }

    public static DirtyAttributeFlusher<?, Object, Object> createIdFlusher(EntityViewManagerImpl evm, ViewType<?> viewType, ViewToEntityMapper viewToEntityMapper) {
        AbstractMethodAttribute<?, ?> idAttribute = (AbstractMethodAttribute<?, ?>) viewType.getIdAttribute();
        String attributeName = idAttribute.getName();
        String attributeMapping = idAttribute.getMapping();
        AttributeAccessor viewAttributeAccessor = Accessors.forViewAttribute(evm, idAttribute, true);
        AttributeAccessor entityAttributeAccessor = Accessors.forEntityMapping(evm, idAttribute);
        Type<?> type = ((com.blazebit.persistence.view.metamodel.SingularAttribute<?, ?>) idAttribute).getType();
        if (idAttribute.isSubview()) {
            return new EmbeddableAttributeFlusher<>(
                    attributeName,
                    null,
                    ID_PARAM_NAME,
                    false,
                    false,
                    true,
                    entityAttributeAccessor,
                    viewAttributeAccessor,
                    viewToEntityMapper
            );
        } else {
            TypeDescriptor typeDescriptor = TypeDescriptor.forType(evm, idAttribute, type);
            return new BasicAttributeFlusher<>(attributeName, attributeMapping, true, false, true, typeDescriptor, null, ID_PARAM_NAME, entityAttributeAccessor, viewAttributeAccessor);
        }
    }

    private DirtyAttributeFlusher<?, Object, Object> createVersionFlusher(EntityViewManagerImpl evm, EntityType<?> entityType, AbstractMethodAttribute<?, ?> versionAttribute) {
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
    
    @Override
    public void executeUpdate(UpdateContext context, MutableStateTrackable updatableProxy) {
        update(context, null, updatableProxy);
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
            flusher.appendUpdateQueryFragment(context, sb, null, null);
            if (sb.length() == initialLength) {
                queryString = null;
                needsOptimisticLocking = false;
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
                idFlusher.flushQuery(context, null, query, updatableProxy, updatableProxy.$$_getId());
            } else {
                query.setParameter(ID_PARAM_NAME, updatableProxy.$$_getId());
            }
            if (needsOptimisticLocking) {
                query.setParameter(VERSION_PARAM_NAME, updatableProxy.$$_getVersion());
            }
        }

        return query;
    }

    private void update(UpdateContext context, Object entity, MutableStateTrackable updatableProxy) {
        if (!rootUpdateAllowed && entity == null) {
            throw new IllegalArgumentException("Updating instances of the view type [" + updatableProxy.getClass().getName() + "] is not allowed because no entity id is known!");
        }

        @SuppressWarnings("unchecked")
        DirtyAttributeFlusher<?, Object, Object> flusher = (DirtyAttributeFlusher<?, Object, Object>) (DirtyAttributeFlusher) getNestedDirtyFlusher(context, updatableProxy, (DirtyAttributeFlusher) null);

        // If nothing is dirty, we don't have to do anything
        if (flusher == null) {
            return;
        }

        try {
            // TODO: Flush strategy AUTO should do flushEntity when entity is known to be fetched already
            if (flushStrategy == FlushStrategy.ENTITY || !flusher.supportsQueryFlush()) {
                flusher.flushEntity(context, entity, updatableProxy, updatableProxy);
            } else {
                Query query = createUpdateQuery(context, updatableProxy, flusher);
                flusher.flushQuery(context, null, query, updatableProxy, updatableProxy);
                if (query != null) {
                    int updated = query.executeUpdate();

                    if (updated != 1) {
                        throw new OptimisticLockException(entity, updatableProxy);
                    }
                }
            }
        } finally {
            context.getInitialStateResetter().addUpdatedView(updatableProxy);
        }
    }

    @Override
    public Object executePersist(UpdateContext context, MutableStateTrackable updatableProxy) {
        Object entity = fullEntityLoader.toEntity(context, null);
        return executePersist(context, entity, updatableProxy);
    }

    @Override
    public Object executePersist(UpdateContext context, Object entity, MutableStateTrackable updatableProxy) {
        fullFlusher.flushEntity(context, entity, updatableProxy, updatableProxy);
        return entity;
    }

    @Override
    public void remove(UpdateContext context, EntityViewProxy entityView) {
        fullFlusher.remove(context, null, entityView, entityView);
    }

    @SuppressWarnings({"unchecked", "checkstyle:methodlength"})
    private static DirtyAttributeFlusher createAttributeFlusher(EntityViewManagerImpl evm, ManagedViewType<?> viewType, FlushStrategy flushStrategy, AbstractMethodAttribute<?, ?> attribute) {
        EntityMetamodel entityMetamodel = evm.getMetamodel().getEntityMetamodel();
        Class<?> entityClass = viewType.getEntityClass();
        String attributeName = attribute.getName();
        String attributeMapping = attribute.getMapping();
        AttributeAccessor entityAttributeAccessor = Accessors.forEntityMapping(evm, attribute);
        String attributeLocation = attribute.getLocation();
        boolean cascadePersist = attribute.isPersistCascaded();
        boolean cascadeUpdate = attribute.isUpdateCascaded();
        boolean optimisticLockProtected = attribute.isOptimisticLockProtected();
        Set<Type<?>> persistAllowedSubtypes = attribute.getPersistCascadeAllowedSubtypes();
        Set<Type<?>> updateAllowedSubtypes = attribute.getUpdateCascadeAllowedSubtypes();

        if (attribute.isCollection()) {
            PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
            InitialValueAttributeAccessor viewAttributeAccessor = Accessors.forMutableViewAttribute(evm, attribute);
            TypeDescriptor elementDescriptor = TypeDescriptor.forType(evm, attribute, pluralAttribute.getElementType());
            boolean collectionUpdatable = attribute.isUpdatable();

            if (attribute instanceof MapAttribute<?, ?, ?>) {
                MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) attribute;
                TypeDescriptor keyDescriptor = TypeDescriptor.forType(evm, attribute, mapAttribute.getKeyType());

                if (collectionUpdatable || keyDescriptor.shouldFlushMutations() || elementDescriptor.shouldFlushMutations() || shouldPassThrough(evm, viewType, attribute)) {
                    MapViewToEntityMapper mapper = new SimpleMapViewToEntityMapper(keyDescriptor.getViewToEntityMapper(), elementDescriptor.getViewToEntityMapper());
                    MapViewToEntityMapper loadOnlyMapper = new SimpleMapViewToEntityMapper(keyDescriptor.getLoadOnlyViewToEntityMapper(), elementDescriptor.getLoadOnlyViewToEntityMapper());

                    MapInstantiator<?, ?> mapInstantiator = attribute.getMapInstantiator();
                    return new MapAttributeFlusher<Object, RecordingMap<Map<?, ?>, ?, ?>>(
                            attributeName,
                            attributeMapping,
                            flushStrategy,
                            entityAttributeAccessor,
                            viewAttributeAccessor,
                            collectionUpdatable,
                            optimisticLockProtected,
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
                    InverseFlusher<Object> inverseFlusher = InverseFlusher.forAttribute(evm, viewType, attribute, elementDescriptor);
                    InverseRemoveStrategy inverseRemoveStrategy = attribute.getInverseRemoveStrategy();

                    CollectionInstantiator collectionInstantiator = attribute.getCollectionInstantiator();
                    if (pluralAttribute.isIndexed()) {
                        return new IndexedListAttributeFlusher<Object, RecordingList<List<?>>>(
                                attributeName,
                                attributeMapping,
                                flushStrategy,
                                entityAttributeAccessor,
                                viewAttributeAccessor,
                                optimisticLockProtected,
                                collectionUpdatable,
                                collectionInstantiator,
                                elementDescriptor,
                                inverseFlusher,
                                inverseRemoveStrategy
                        );
                    } else {
                        return new CollectionAttributeFlusher(
                                attributeName,
                                attributeMapping,
                                flushStrategy,
                                entityAttributeAccessor,
                                viewAttributeAccessor,
                                optimisticLockProtected,
                                collectionUpdatable,
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
        } else if (attribute.isSubview()) {
            boolean shouldFlushUpdates = cascadeUpdate && !updateAllowedSubtypes.isEmpty();
            boolean shouldFlushPersists = cascadePersist && !persistAllowedSubtypes.isEmpty();
            AttributeAccessor viewAttributeAccessor;

            ManagedViewTypeImplementor<?> subviewType = getManagedViewType(attribute);
            boolean passThrough = false;

            if (attribute.isUpdatable() || shouldFlushUpdates || (passThrough = shouldPassThrough(evm, viewType, attribute))) {
                // TODO: shouldn't this be done for any flat view? or are updatable flat views for entity types disallowed?
                if (entityMetamodel.getEntity(subviewType.getEntityClass()) == null) {
                    viewAttributeAccessor = Accessors.forViewAttribute(evm, attribute, true);
                    // A singular attribute where the subview refers to an embeddable type
                    ViewToEntityMapper viewToEntityMapper = new EmbeddableUpdaterBasedViewToEntityMapper(
                            attributeLocation,
                            evm,
                            subviewType.getJavaType(),
                            persistAllowedSubtypes,
                            updateAllowedSubtypes,
                            new ReferenceEntityLoader(evm, subviewType, createViewIdMapper(evm, subviewType)),
                            shouldFlushPersists
                    );

                    boolean supportsQueryFlush = evm.getJpaProvider().supportsUpdateSetEmbeddable();
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
                    ViewTypeImpl<?> attributeViewType = (ViewTypeImpl<?>) subviewType;
                    viewAttributeAccessor = Accessors.forMutableViewAttribute(evm, attribute);
                    AttributeAccessor subviewIdAccessor = Accessors.forViewId(evm, attributeViewType, true);
                    ViewToEntityMapper viewToEntityMapper;
                    boolean fetch = shouldFlushUpdates;
                    String parameterName = null;
                    String updateFragment = null;

                    if (attribute.isUpdatable()) {
                        SingularAttribute<?, ?> entityIdAttribute = JpaMetamodelUtils.getIdAttribute(evm.getMetamodel().getEntityMetamodel().entity(entityClass));
                        String idAttributeMapping = attributeMapping + "." + entityIdAttribute.getName();
                        viewToEntityMapper = createViewToEntityMapper(attributeLocation, evm, attributeViewType, cascadePersist, cascadeUpdate, persistAllowedSubtypes, updateAllowedSubtypes);
                        parameterName = attributeName;
                        updateFragment = idAttributeMapping;
                    } else {
                        if (shouldFlushUpdates) {
                            viewToEntityMapper = new UpdaterBasedViewToEntityMapper(
                                    attributeLocation,
                                    evm,
                                    subviewType.getJavaType(),
                                    persistAllowedSubtypes,
                                    updateAllowedSubtypes,
                                    new ReferenceEntityLoader(evm, subviewType, createViewIdMapper(evm, subviewType)),
                                    Accessors.forViewId(evm, attributeViewType, true),
                                    shouldFlushPersists
                            );
                        } else if (shouldFlushPersists) {
                            viewToEntityMapper = new CreateOnlyViewToEntityMapper(
                                    attributeLocation,
                                    evm,
                                    subviewType.getJavaType(),
                                    persistAllowedSubtypes,
                                    updateAllowedSubtypes,
                                    null,
                                    null,
                                    shouldFlushPersists
                            );
                        } else {
                            viewToEntityMapper = new LoadOrPersistViewToEntityMapper(
                                    attributeLocation,
                                    evm,
                                    subviewType.getJavaType(),
                                    persistAllowedSubtypes,
                                    updateAllowedSubtypes,
                                    new ReferenceEntityLoader(evm, subviewType, createViewIdMapper(evm, subviewType)),
                                    null,
                                    shouldFlushPersists
                            );
                        }
                    }

                    return new SubviewAttributeFlusher<>(
                            attributeName,
                            attributeMapping,
                            optimisticLockProtected,
                            attribute.isUpdatable(),
                            fetch,
                            updateFragment,
                            parameterName,
                            passThrough,
                            entityAttributeAccessor,
                            viewAttributeAccessor,
                            subviewIdAccessor,
                            viewToEntityMapper
                    );
                }
            } else {
                return null;
            }
        } else {
            BasicType<?> attributeType = (BasicType<?>) ((com.blazebit.persistence.view.metamodel.SingularAttribute<?, ?>) attribute).getType();
            TypeDescriptor elementDescriptor = TypeDescriptor.forType(evm, attribute, attributeType);
            // Basic attributes like String, Integer but also JPA managed types
            boolean updatable = attribute.isUpdatable();

            if (updatable || elementDescriptor.shouldFlushMutations() || shouldPassThrough(evm, viewType, attribute)) {
                // Basic attributes can normally be updated by queries
                String parameterName = attributeName;
                String updateFragment = attributeMapping;

                // When wanting to read the actual value of non-updatable attributes or writing values to attributes we need the view attribute accessor
                // Whenever we merge or persist, we are going to need that
                AttributeAccessor viewAttributeAccessor;
                if (elementDescriptor.shouldFlushMutations()) {
                    viewAttributeAccessor = Accessors.forMutableViewAttribute(evm, attribute);
                } else {
                    viewAttributeAccessor = Accessors.forViewAttribute(evm, attribute, true);
                }

                boolean supportsQueryFlush = !elementDescriptor.isJpaEmbeddable() || evm.getJpaProvider().supportsUpdateSetEmbeddable();
                return new BasicAttributeFlusher<>(attributeName, attributeMapping, supportsQueryFlush, optimisticLockProtected, updatable, elementDescriptor, updateFragment, parameterName, entityAttributeAccessor, viewAttributeAccessor);
            } else {
                return null;
            }
        }
    }

    private static boolean shouldPassThrough(EntityViewManagerImpl evm, ManagedViewType<?> viewType, AbstractMethodAttribute<?, ?> attribute) {
        // For an attribute being eligible for pass through, the declaring view must be for an embeddable type
        // and the attribute must be update mappable
        return evm.getMetamodel().getEntityMetamodel().getEntity(viewType.getEntityClass()) == null
                && attribute.isUpdateMappable();
    }

    private static Map<String, Map<?, ?>> getFetchGraph(String[] fetches, String attributeMapping, ManagedType<?> managedType) {
        Map<String, Map<?, ?>> fetchGraph = null;
        boolean isEntity = managedType instanceof EntityType<?>;
        if (managedType != null && (isEntity || fetches.length > 0)) {
            fetchGraph = new HashMap<>();
            Map<String, Map<?, ?>> subGraph = new HashMap<>();
            String partPrefix;

            // Entity types, contrary to embeddable types, can be join fetched
            if (isEntity) {
                fetchGraph.put(attributeMapping, subGraph);
                partPrefix = "";
            } else {
                // If this is an embeddable type, the subgraph relations get prefixed with the embeddable attribute name
                fetchGraph = subGraph;
                partPrefix = attributeMapping + ".";
            }

            for (String subFetch : fetches) {
                String[] parts = subFetch.split("\\.");
                Map<String, Map<?, ?>> currentSubGraph = subGraph;

                for (String part : parts) {
                    String subPath = partPrefix + part;
                    Map<?, ?> map = currentSubGraph.get(subPath);
                    if (map == null) {
                        map = new HashMap<>();
                        currentSubGraph.put(subPath, map);
                    }

                    currentSubGraph = (Map<String, Map<?, ?>>) map;
                }
            }
        }
        return fetchGraph;
    }

    private static ViewToEntityMapper createViewToEntityMapper(String attributeLocation, EntityViewManagerImpl evm, ViewType<?> viewType, boolean cascadePersist, boolean cascadeUpdate, Set<Type<?>> persistAllowedSubtypes, Set<Type<?>> updateAllowedSubtypes) {
        EntityLoader entityLoader = new ReferenceEntityLoader(evm, viewType, createViewIdMapper(evm, viewType));

        AttributeAccessor viewIdAccessor = null;
        if (viewType instanceof ViewType<?>) {
            viewIdAccessor = Accessors.forViewId(evm, viewType, true);
        }
        Class<?> viewTypeClass = viewType.getJavaType();
        boolean mutable = viewType.isUpdatable() || viewType.isCreatable() || !persistAllowedSubtypes.isEmpty() || !updateAllowedSubtypes.isEmpty();
        if (!mutable || !cascadeUpdate) {
            return new LoadOrPersistViewToEntityMapper(
                    attributeLocation,
                    evm,
                    viewTypeClass,
                    persistAllowedSubtypes,
                    updateAllowedSubtypes,
                    entityLoader,
                    viewIdAccessor,
                    cascadePersist);
        }

        return new UpdaterBasedViewToEntityMapper(
                attributeLocation,
                evm,
                viewTypeClass,
                persistAllowedSubtypes,
                updateAllowedSubtypes,
                entityLoader,
                viewIdAccessor,
                cascadePersist
        );
    }

    private static ManagedViewTypeImplementor<?> getManagedViewType(MethodAttribute<?, ?> methodAttribute) {
        if (methodAttribute.getAttributeType() == Attribute.AttributeType.SINGULAR) {
            return (ManagedViewTypeImplementor<?>) ((com.blazebit.persistence.view.metamodel.SingularAttribute<?, ?>) methodAttribute).getType();
        } else {
            return (ManagedViewTypeImplementor<?>) ((com.blazebit.persistence.view.metamodel.PluralAttribute<?, ?, ?>) methodAttribute).getElementType();
        }
    }

}
