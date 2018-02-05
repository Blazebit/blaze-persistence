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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.DeleteCriteriaBuilder;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.OptimisticLockException;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.change.DirtyChecker;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.impl.entity.EntityLoader;
import com.blazebit.persistence.view.impl.entity.EntityTupleizer;
import com.blazebit.persistence.view.impl.entity.ReferenceEntityLoader;
import com.blazebit.persistence.view.impl.mapper.ViewMapper;
import com.blazebit.persistence.view.impl.proxy.DirtyTracker;
import com.blazebit.persistence.view.impl.update.EntityViewUpdaterImpl;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.entity.FlusherBasedEntityLoader;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.proxy.DirtyStateTrackable;
import com.blazebit.persistence.view.impl.proxy.MutableStateTrackable;
import com.blazebit.persistence.view.spi.type.BasicDirtyTracker;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CompositeAttributeFlusher extends CompositeAttributeFetchGraphNode<CompositeAttributeFlusher, DirtyAttributeFlusher<?, Object, Object>> implements DirtyAttributeFlusher<CompositeAttributeFlusher, Object, Object> {

    private static final int FEATURE_SUPPORTS_QUERY_FLUSH = 0;
    private static final int FEATURE_HAS_PASS_THROUGH_FLUSHER = 1;
    private static final int FEATURE_IS_ANY_OPTIMISTIC_LOCK_PROTECTED = 2;
    private static final UnmappedAttributeCascadeDeleter[] EMPTY = new UnmappedAttributeCascadeDeleter[0];

    private final Class<?> entityClass;
    private final boolean persistable;
    private final ViewMapper<Object, Object> persistViewMapper;
    private final javax.persistence.metamodel.SingularAttribute<?, ?> jpaIdAttribute;
    private final ViewToEntityMapper viewIdMapper;
    private final AttributeAccessor viewIdAccessor;
    private final AttributeAccessor entityIdAccessor;
    private final EntityTupleizer tupleizer;
    private final ObjectBuilder<Object> idViewBuilder;
    private final DirtyAttributeFlusher<?, Object, Object> idFlusher;
    private final DirtyAttributeFlusher<?, Object, Object> versionFlusher;
    // split in pre- and post-object remove based on requiresDeleteCascadeAfterRemove()
    private final UnmappedAttributeCascadeDeleter[] unmappedPreRemoveCascadeDeleters;
    private final UnmappedAttributeCascadeDeleter[] unmappedPostRemoveCascadeDeleters;
    private final FlushMode flushMode;
    private final FlushStrategy flushStrategy;
    private final EntityLoader entityLoader;
    private final EntityLoader referenceEntityLoader;
    private final String deleteQuery;
    private final String versionedDeleteQuery;
    private final boolean supportsQueryFlush;
    private final boolean hasPassThroughFlushers;
    private final boolean optimisticLockProtected;

    private final Object element;

    @SuppressWarnings("unchecked")
    public CompositeAttributeFlusher(Class<?> viewType, Class<?> entityClass, ManagedType<?> managedType, boolean persistable, ViewMapper<Object, Object> persistViewMapper, SingularAttribute<?, ?> jpaIdAttribute, AttributeAccessor entityIdAccessor,
                                     ViewToEntityMapper viewIdMapper, AttributeAccessor viewIdAccessor, EntityTupleizer tupleizer, ObjectBuilder<Object> idViewBuilder, DirtyAttributeFlusher<?, Object, Object> idFlusher,
                                     DirtyAttributeFlusher<?, Object, Object> versionFlusher, UnmappedAttributeCascadeDeleter[] cascadeDeleteUnmappedFlushers, DirtyAttributeFlusher[] flushers, FlushMode flushMode, FlushStrategy flushStrategy) {
        super(viewType, flushers, null);
        this.entityClass = entityClass;
        this.persistable = persistable;
        this.persistViewMapper = persistViewMapper;
        this.jpaIdAttribute = jpaIdAttribute;
        this.viewIdMapper = viewIdMapper;
        this.viewIdAccessor = viewIdAccessor;
        this.entityIdAccessor = entityIdAccessor;
        this.tupleizer = tupleizer;
        this.idViewBuilder = idViewBuilder;
        this.idFlusher = idFlusher;
        this.versionFlusher = versionFlusher;
        this.unmappedPreRemoveCascadeDeleters = getPreRemoveFlushers(cascadeDeleteUnmappedFlushers);
        this.unmappedPostRemoveCascadeDeleters = getPostRemoveFlushers(cascadeDeleteUnmappedFlushers);
        this.flushMode = flushMode;
        this.flushStrategy = flushStrategy;
        this.entityLoader = new FlusherBasedEntityLoader(entityClass, jpaIdAttribute, viewIdMapper, entityIdAccessor, flushers);
        this.referenceEntityLoader = new ReferenceEntityLoader(entityClass, jpaIdAttribute, viewIdMapper, entityIdAccessor);
        this.deleteQuery = createDeleteQuery(managedType, jpaIdAttribute);
        this.versionedDeleteQuery = createVersionedDeleteQuery(deleteQuery, versionFlusher);
        boolean[] features = determineFeatures(flushers);
        this.supportsQueryFlush = flushStrategy != FlushStrategy.ENTITY && features[FEATURE_SUPPORTS_QUERY_FLUSH];
        this.hasPassThroughFlushers = features[FEATURE_HAS_PASS_THROUGH_FLUSHER];
        this.optimisticLockProtected = features[FEATURE_IS_ANY_OPTIMISTIC_LOCK_PROTECTED];
        this.element = null;
    }

    private CompositeAttributeFlusher(CompositeAttributeFlusher original, DirtyAttributeFlusher[] flushers, Object element, boolean persist) {
        super(original.viewType, original.attributeIndexMapping, flushers, persist);
        this.entityClass = original.entityClass;
        this.persistable = original.persistable;
        this.persistViewMapper = original.persistViewMapper;
        this.jpaIdAttribute = original.jpaIdAttribute;
        this.viewIdMapper = original.viewIdMapper;
        this.viewIdAccessor = original.viewIdAccessor;
        this.entityIdAccessor = original.entityIdAccessor;
        this.tupleizer = original.tupleizer;
        this.idViewBuilder = original.idViewBuilder;
        this.idFlusher = original.idFlusher;
        this.versionFlusher = original.versionFlusher;
        this.unmappedPreRemoveCascadeDeleters = original.unmappedPreRemoveCascadeDeleters;
        this.unmappedPostRemoveCascadeDeleters = original.unmappedPostRemoveCascadeDeleters;
        this.flushMode = original.flushMode;
        this.flushStrategy = original.flushStrategy;
        this.entityLoader = new FlusherBasedEntityLoader(entityClass, jpaIdAttribute, viewIdMapper, entityIdAccessor, flushers);
        this.referenceEntityLoader = original.referenceEntityLoader;
        this.deleteQuery = original.deleteQuery;
        this.versionedDeleteQuery = original.versionedDeleteQuery;
        this.supportsQueryFlush = supportsQueryFlush(flushers);
        this.hasPassThroughFlushers = original.hasPassThroughFlushers;
        this.optimisticLockProtected = original.optimisticLockProtected;
        this.element = element;
    }

    private UnmappedAttributeCascadeDeleter[] getPreRemoveFlushers(UnmappedAttributeCascadeDeleter[] cascadeDeleteUnmappedFlushers) {
        if (cascadeDeleteUnmappedFlushers == null) {
            return EMPTY;
        }
        List<UnmappedAttributeCascadeDeleter> flusherList = new ArrayList<>(cascadeDeleteUnmappedFlushers.length);
        for (UnmappedAttributeCascadeDeleter flusher : cascadeDeleteUnmappedFlushers) {
            if (!flusher.requiresDeleteCascadeAfterRemove()) {
                flusherList.add(flusher);
            }
        }

        return flusherList.toArray(new UnmappedAttributeCascadeDeleter[flusherList.size()]);
    }

    private UnmappedAttributeCascadeDeleter[] getPostRemoveFlushers(UnmappedAttributeCascadeDeleter[] cascadeDeleteUnmappedFlushers) {
        if (cascadeDeleteUnmappedFlushers == null) {
            return EMPTY;
        }
        List<UnmappedAttributeCascadeDeleter> flusherList = new ArrayList<>(cascadeDeleteUnmappedFlushers.length);
        for (UnmappedAttributeCascadeDeleter flusher : cascadeDeleteUnmappedFlushers) {
            if (flusher.requiresDeleteCascadeAfterRemove()) {
                flusherList.add(flusher);
            }
        }

        return flusherList.toArray(new UnmappedAttributeCascadeDeleter[flusherList.size()]);
    }

    private static String createDeleteQuery(ManagedType<?> managedType, SingularAttribute<?, ?> jpaIdAttribute) {
        if (managedType instanceof EntityType<?> && jpaIdAttribute != null) {
            return "DELETE FROM " + ((EntityType) managedType).getName() + " e WHERE e." + jpaIdAttribute.getName() + " = :" + EntityViewUpdaterImpl.ID_PARAM_NAME;
        }

        return null;
    }

    private static String createVersionedDeleteQuery(String deleteQuery, DirtyAttributeFlusher versionFlusher) {
        if (deleteQuery != null && versionFlusher != null) {
            return deleteQuery + " AND e." + versionFlusher.getAttributeName() + " = :" + EntityViewUpdaterImpl.VERSION_PARAM_NAME;
        }

        return null;
    }

    private static boolean[] determineFeatures(DirtyAttributeFlusher[] flushers) {
        boolean hasPassThroughFlusher = false;
        boolean supportsQueryFlush = true;
        boolean anyOptimisticLockProtected = false;
        for (int i = 0; i < flushers.length; i++) {
            final DirtyAttributeFlusher<?, ?, ?> f = flushers[i];
            if (f != null) {
                hasPassThroughFlusher = hasPassThroughFlusher || f.isPassThrough();
                supportsQueryFlush = supportsQueryFlush && f.supportsQueryFlush();
                anyOptimisticLockProtected = anyOptimisticLockProtected || f.isOptimisticLockProtected();
            }
        }

        boolean[] features = new boolean[3];
        features[FEATURE_HAS_PASS_THROUGH_FLUSHER] = hasPassThroughFlusher;
        features[FEATURE_SUPPORTS_QUERY_FLUSH] = supportsQueryFlush;
        features[FEATURE_IS_ANY_OPTIMISTIC_LOCK_PROTECTED] = anyOptimisticLockProtected;
        return features;
    }

    private boolean supportsQueryFlush(DirtyAttributeFlusher[] flushers) {
        for (int i = 0; i < flushers.length; i++) {
            if (flushers[i] != null && !flushers[i].supportsQueryFlush()) {
                return false;
            }
        }

        return true;
    }

    public boolean hasVersionFlusher() {
        return versionFlusher != null;
    }

    @Override
    public Object cloneDeep(Object view, Object oldValue, Object newValue) {
        return newValue;
    }

    @Override
    public void appendUpdateQueryFragment(UpdateContext context, StringBuilder sb, String mappingPrefix, String parameterPrefix) {
        int clauseEndIndex = sb.length();
        if (optimisticLockProtected && versionFlusher != null) {
            versionFlusher.appendUpdateQueryFragment(context, sb, mappingPrefix, parameterPrefix);
            // If something was appended, we also append a comma
            if (clauseEndIndex != sb.length()) {
                clauseEndIndex = sb.length();
                sb.append(", ");
            }
        }

        for (int i = 0; i < flushers.length; i++) {
            if (flushers[i] != null) {
                int endIndex = sb.length();
                flushers[i].appendUpdateQueryFragment(context, sb, mappingPrefix, parameterPrefix);

                // If something was appended, we also append a comma
                if (endIndex != sb.length()) {
                    clauseEndIndex = sb.length();
                    sb.append(", ");
                }
            }
        }

        if (clauseEndIndex + 2 == sb.length()) {
            // Remove the last comma
            sb.setLength(clauseEndIndex);
        }
    }

    @Override
    public boolean supportsQueryFlush() {
        return supportsQueryFlush;
    }

    @Override
    public void flushQuery(UpdateContext context, String parameterPrefix, Query query, Object view, Object value) {
        if (element != null) {
            value = element;
        } else if (value == null || !(value instanceof MutableStateTrackable)) {
            return;
        }

        MutableStateTrackable element = (MutableStateTrackable) value;
        // Already removed objects or objects without a parent can't be flushed
        // The root object, which is given when view == value, is the exception
        if (context.isRemovedObject(element) || !element.$$_hasParent() && view != value) {
            return;
        }
        // Object persisting only works via entity flushing
        boolean shouldPersist = persist == Boolean.TRUE || persist == null && element.$$_isNew();
        if (shouldPersist) {
            flushEntity(context, null, value, value, null);
            return;
        }

        if (optimisticLockProtected && versionFlusher != null) {
            context.getInitialStateResetter().addVersionedView(element, element.$$_getVersion());
            versionFlusher.flushQuery(context, parameterPrefix, query, value, element.$$_getVersion());
        }

        Object[] state = element.$$_getMutableState();

        if (value instanceof DirtyStateTrackable) {
            Object[] initialState = ((DirtyStateTrackable) value).$$_getInitialState();
            context.getInitialStateResetter().addState(initialState, initialState.clone());

            for (int i = 0; i < state.length; i++) {
                if (flushers[i] != null) {
                    initialState[i] = flushers[i].cloneDeep(value, initialState[i], state[i]);
                    flushers[i].flushQuery(context, parameterPrefix, query, value, state[i]);
                }
            }
        } else {
            for (int i = 0; i < state.length; i++) {
                if (flushers[i] != null) {
                    flushers[i].flushQuery(context, parameterPrefix, query, value, state[i]);
                }
            }
        }

        for (int i = state.length; i < flushers.length; i++) {
            if (flushers[i] != null) {
                flushers[i].flushQuery(context, parameterPrefix, query, value, flushers[i].getViewAttributeAccessor().getValue(value));
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean flushEntity(UpdateContext context, Object entity, Object view, Object value, Runnable postReplaceListener) {
        if (element != null) {
            value = element;
        }
        MutableStateTrackable updatableProxy = (MutableStateTrackable) value;
        // Already removed objects or objects without a parent can't be flushed
        // The root object, which is given when view == value, is the exception
        if (context.isRemovedObject(updatableProxy) || !updatableProxy.$$_hasParent() && view != value) {
            return false;
        }

        final boolean shouldPersist = persist == Boolean.TRUE || persist == null && updatableProxy.$$_isNew();
        final boolean doPersist = shouldPersist && persistable;
        List<Integer> deferredFlushers = null;
        boolean successful = false;
        try {
            Object id = updatableProxy.$$_getId();
            int parentIndex = updatableProxy.$$_getParentIndex();
            DirtyTracker parent = updatableProxy.$$_getParent();
            RecordingCollection<?, Object> recordingCollection = null;
            RecordingMap<?, Object, Object> recordingMap = null;
            Object removedValue = null;
            Set<Object> removedKeys = null;

            if (doPersist) {
                // In case of nested attributes, the entity instance we get is the container of the attribute
                if (entity == null || !entityClass.isInstance(entity)) {
                    entity = entityLoader.toEntity(context, null);
                }
                // If the parent is a hash based collection, or the view is re-mapped to a different type, remove before setting the id/re-mapping
                // There are two cases here, either we are in full flushing and we can get a RecordingIterator via getCurrentIterator
                // Or we are in the elementFlusher case where we don't iterate through the backing collection and thus can operate on the backing collection directly
                if (parent != null) {
                    if (parent instanceof RecordingCollection<?, ?> && ((recordingCollection = (RecordingCollection<?, Object>) parent).isHashBased() || persistViewMapper != null)) {
                        if (recordingCollection.getCurrentIterator() == null) {
                            recordingCollection.getDelegate().remove(updatableProxy);
                        } else {
                            recordingCollection.getCurrentIterator().replace();
                        }
                    } else if (parent instanceof RecordingMap<?, ?, ?> && (persistViewMapper != null || updatableProxy.$$_getParentIndex() == 1 && (recordingMap = (RecordingMap<?, Object, Object>) parent).isHashBased())) {
                        recordingMap = (RecordingMap<?, Object, Object>) parent;
                        // Parent index 1 in a recording map means it is part of the key
                        if (updatableProxy.$$_getParentIndex() == 1) {
                            if (recordingMap.getCurrentIterator() == null) {
                                removedValue = recordingMap.getDelegate().remove(updatableProxy);
                            } else {
                                removedValue = recordingMap.getCurrentIterator().replace();
                            }
                        } else {
                            if (removedKeys == null) {
                                removedKeys = new HashSet<>();
                            }
                            // TODO: replaceValue currently only handles the current value, which is inconsistent regarding what we do in the elementFlusher case
                            // Not sure if a creatable view should be allowed to occur multiple times in the map as value..
                            if (recordingMap.getCurrentIterator() == null) {
                                for (Map.Entry<Object, Object> entry : recordingMap.getDelegate().entrySet()) {
                                    if (entry.getValue().equals(updatableProxy)) {
                                        removedKeys.add(entry.getKey());
                                    }
                                }
                            } else {
                                recordingMap.getCurrentIterator().replaceValue(removedKeys);
                            }
                        }
                    }
                }

                // I know, that this is likely the ugliest hack ever, but to fix this properly would require a major redesign of the flusher handling which is too much work for this version
                // A version 2.0 or 3.0 might improve on this when redesigning for operation queueing
                if (postReplaceListener != null) {
                    postReplaceListener.run();
                }

                if (id != null) {
                    idFlusher.flushEntity(context, entity, updatableProxy, id, null);
                }
            } else {
                // In case of nested attributes, the entity instance we get is the container of the attribute
                if (entity == null || !entityClass.isInstance(entity)) {
                    entity = entityLoader.toEntity(context, id);
                }
            }
            Object[] state = updatableProxy.$$_getMutableState();
            boolean wasDirty = false;

            if (updatableProxy instanceof DirtyStateTrackable) {
                Object[] initialState = ((DirtyStateTrackable) updatableProxy).$$_getInitialState();
                context.getInitialStateResetter().addState(initialState, initialState.clone());

                if (doPersist) {
                    for (int i = 0; i < state.length; i++) {
                        final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                        if (flusher != null) {
                            initialState[i] = flusher.cloneDeep(value, initialState[i], state[i]);
                            if (flusher.requiresFlushAfterPersist(state[i])) {
                                if (deferredFlushers == null) {
                                    deferredFlushers = new ArrayList<>();
                                }
                                deferredFlushers.add(i);
                                wasDirty = true;
                            } else {
                                wasDirty |= flusher.flushEntity(context, entity, value, state[i], null);
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < state.length; i++) {
                        final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                        if (flusher != null) {
                            initialState[i] = flusher.cloneDeep(value, initialState[i], state[i]);
                            wasDirty |= flusher.flushEntity(context, entity, value, state[i], null);
                        }
                    }
                }
            } else {
                if (doPersist) {
                    for (int i = 0; i < state.length; i++) {
                        final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                        if (flusher != null) {
                            wasDirty |= flusher.flushEntity(context, entity, value, state[i], null);
                        }
                    }
                } else {
                    for (int i = 0; i < state.length; i++) {
                        final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                        if (flusher != null) {
                            if (flusher.requiresFlushAfterPersist(state[i])) {
                                if (deferredFlushers == null) {
                                    deferredFlushers = new ArrayList<>();
                                }
                                deferredFlushers.add(i);
                                wasDirty = true;
                            } else {
                                wasDirty |= flusher.flushEntity(context, entity, value, state[i], null);
                            }
                        }
                    }
                }
            }

            // Pass through flushers
            for (int i = state.length; i < flushers.length; i++) {
                final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                if (flushers[i] != null) {
                    wasDirty |= flusher.flushEntity(context, entity, value, flusher.getViewAttributeAccessor().getValue(value), null);
                }
            }
            if (versionFlusher != null && wasDirty) {
                context.getInitialStateResetter().addVersionedView(updatableProxy, updatableProxy.$$_getVersion());
                versionFlusher.flushEntity(context, entity, value, updatableProxy.$$_getVersion(), null);
            }
            if (doPersist) {
                // If the class of the object is an entity, we persist the object
                context.getEntityManager().persist(entity);
                id = entityLoader.getEntityId(context, entity);
                if (tupleizer != null) {
                    Object[] tuple = tupleizer.tupleize(id);
                    id = idViewBuilder.build(tuple);
                }
                viewIdAccessor.setValue(updatableProxy, id);
                Object newObject = updatableProxy;
                if (persistViewMapper != null) {
                    newObject = persistViewMapper.map(newObject);
                }
                if (recordingCollection != null && (recordingCollection.isHashBased() || persistViewMapper != null)) {
                    // Reset the parent accordingly
                    updatableProxy.$$_unsetParent();
                    if (newObject instanceof BasicDirtyTracker) {
                        ((BasicDirtyTracker) newObject).$$_setParent(parent, parentIndex);
                    }
                    if (recordingCollection.getCurrentIterator() == null) {
                        recordingCollection.getDelegate().add(newObject);
                    } else {
                        recordingCollection.getCurrentIterator().add(newObject);
                    }
                } else if (recordingMap != null && (persistViewMapper != null || updatableProxy.$$_getParentIndex() == 1 && recordingMap.isHashBased())) {
                    // Reset the parent accordingly
                    updatableProxy.$$_unsetParent();
                    if (newObject instanceof BasicDirtyTracker) {
                        ((BasicDirtyTracker) newObject).$$_setParent(parent, parentIndex);
                    }
                    if (updatableProxy.$$_getParentIndex() == 1) {
                        if (recordingMap.getCurrentIterator() == null) {
                            recordingMap.getDelegate().put(newObject, removedValue);
                        } else {
                            recordingMap.getCurrentIterator().add(newObject, removedValue);
                        }
                    } else {
                        for (Object removedKey : removedKeys) {
                            if (recordingMap.getCurrentIterator() == null) {
                                recordingMap.getDelegate().put(removedKey, newObject);
                            } else {
                                recordingMap.getCurrentIterator().add(removedKey, newObject);
                            }
                        }
                    }
                } else if (parent != null && persistViewMapper != null) {
                    // In case of a singular attribute, we replace the mutable state object to signal the parent flusher
                    // SubviewAttributeFlusher is the parent, that uses this object for setting the actual and initial state
                    ((MutableStateTrackable) parent).$$_getMutableState()[updatableProxy.$$_getParentIndex()] = newObject;
                    updatableProxy.$$_unsetParent();
                }
            }
            successful = true;
            return wasDirty;
        } finally {
            if (shouldPersist) {
                context.getInitialStateResetter().addPersistedView(updatableProxy);

                if (successful && deferredFlushers != null) {
                    Object[] state = updatableProxy.$$_getMutableState();
                    for (int i = 0; i < deferredFlushers.size(); i++) {
                        final int index = deferredFlushers.get(i);
                        final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[index];
                        flusher.flushEntity(context, entity, value, state[index], null);
                    }
                }
            }
        }
    }

    @Override
    public List<PostRemoveDeleter> remove(UpdateContext context, Object entity, Object view, Object value) {
        if (value instanceof MutableStateTrackable) {
            MutableStateTrackable updatableProxy = (MutableStateTrackable) value;

            // Only remove objects that are
            // 1. Persistable i.e. of an entity type that can be removed
            // 2. Have no parent
            // 3. Haven't been removed yet
            // 4. Aren't new i.e. only existing objects, no need to delete object that hasn't been persisted yet
            if (persistable && !updatableProxy.$$_hasParent() && context.addRemovedObject(value) && !updatableProxy.$$_isNew()) {
                Object[] state = updatableProxy.$$_getMutableState();
                List<PostRemoveDeleter> postRemoveDeleters = new ArrayList<>();

                for (int i = 0; i < state.length; i++) {
                    final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                    if (flusher != null && !flusher.requiresDeleteCascadeAfterRemove()) {
                        postRemoveDeleters.addAll(flusher.remove(context, entity, value, state[i]));
                    }
                }

                remove(context, entity, updatableProxy, updatableProxy.$$_getId(), updatableProxy.$$_getVersion(), false);

                for (PostRemoveDeleter postRemoveDeleter : postRemoveDeleters) {
                    postRemoveDeleter.execute(context);
                }

                for (int i = 0; i < state.length; i++) {
                    final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                    if (flusher != null && flusher.requiresDeleteCascadeAfterRemove()) {
                        flusher.remove(context, entity, value, state[i]);
                    }
                }
            }
        } else {
            EntityViewProxy entityView = (EntityViewProxy) value;
            if (context.addRemovedObject(value)) {
                remove(context, entity, entityView, entityView.$$_getId(), entityView.$$_getVersion(), false);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void remove(UpdateContext context, Object id) {
        remove(context, null, null, id, null, true);
    }

    @Override
    public void removeFromEntity(UpdateContext context, Object entity) {
        // A composite flusher needs to be wrapped in a subview or collection flusher
        throw new UnsupportedOperationException();
    }

    private void remove(UpdateContext context, Object entity, Object view, Object id, Object version, boolean cascadeMappedDeletes) {
        if (flushStrategy == FlushStrategy.ENTITY) {
            if (entity == null) {
                entity = referenceEntityLoader.toEntity(context, id);
            }

            // Ensure the entity version is the expected one
            if (version != null && versionFlusher != null) {
                versionFlusher.remove(context, entity, null, version);
            }

            if (cascadeMappedDeletes) {
                for (int i = 0; i < flushers.length; i++) {
                    final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                    if (flusher != null && !flusher.requiresDeleteCascadeAfterRemove()) {
                        flusher.removeFromEntity(context, entity);
                    }
                }
            }

            context.getEntityManager().remove(entity);

            if (cascadeMappedDeletes) {
                // nested cascades
                for (int i = 0; i < flushers.length; i++) {
                    final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                    if (flusher != null && flusher.requiresDeleteCascadeAfterRemove()) {
                        flusher.removeFromEntity(context, entity);
                    }
                }
            }
        } else {
            // Query flush strategy

            // TODO: in the future, we could try to aggregate deletes into modification CTEs if we know there are no cycles

            // We only need to cascade delete unmapped attributes for query flushing since entity flushing takes care of that for us
            for (int i = 0; i < unmappedPreRemoveCascadeDeleters.length; i++) {
                unmappedPreRemoveCascadeDeleters[i].removeByOwnerId(context, id);
            }

            Object[] returnedValues = null;
            List<PostRemoveDeleter> postRemoveDeleters = new ArrayList<>();
            if (cascadeMappedDeletes) {
                for (int i = 0; i < flushers.length; i++) {
                    final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                    if (flusher != null && !flusher.requiresDeleteCascadeAfterRemove()) {
                        postRemoveDeleters.addAll(flusher.removeByOwnerId(context, id));
                    }
                }
            }

            boolean doDelete = true;
            // need to "return" the values from the delete query for the post deleters since the values aren't available after executing the delete query
            if (cascadeMappedDeletes || unmappedPostRemoveCascadeDeleters.length != 0) {
                List<String> returningAttributes = new ArrayList<>();
                for (int i = 0; i < unmappedPostRemoveCascadeDeleters.length; i++) {
                    returningAttributes.add(unmappedPostRemoveCascadeDeleters[i].getAttributeValuePath());
                }
                if (cascadeMappedDeletes) {
                    for (int i = 0; i < flushers.length; i++) {
                        final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                        if (flusher != null && flusher.requiresDeleteCascadeAfterRemove()) {
                            String elementIdAttributeName = flushers[i].getElementIdAttributeName();
                            if (elementIdAttributeName != null) {
                                returningAttributes.add(elementIdAttributeName);
                            }
                        }
                    }
                }

                if (!returningAttributes.isEmpty()) {
                    // If the dbms supports it, we use the returning feature to do this
                    if (context.getEntityViewManager().getDbmsDialect().supportsReturningColumns()) {
                        DeleteCriteriaBuilder<?> cb = context.getEntityViewManager().getCriteriaBuilderFactory().delete(context.getEntityManager(), entityClass);
                        cb.where(idFlusher.getAttributeName()).eq(id);
                        if (version != null && versionFlusher != null) {
                            cb.where(versionFlusher.getAttributeName()).eq(version);
                        }

                        ReturningResult<Tuple> result = cb.executeWithReturning(returningAttributes.toArray(new String[returningAttributes.size()]));
                        if (version != null && versionFlusher != null) {
                            if (result.getUpdateCount() != 1) {
                                throw new OptimisticLockException(entity, view);
                            }
                        }
                        returnedValues = result.getLastResult().toArray();
                        doDelete = false;
                    } else {
                        // Otherwise we query the attributes
                        CriteriaBuilder<Object[]> cb = context.getEntityViewManager().getCriteriaBuilderFactory().create(context.getEntityManager(), Object[].class);
                        cb.from(entityClass);
                        cb.where(idFlusher.getAttributeName()).eq(id);
                        for (String attribute : returningAttributes) {
                            cb.select(attribute);
                        }
                        Object result = cb.getSingleResult();
                        // Hibernate might return the object itself although we specified that we want an Object[] return...
                        if (result instanceof Object[]) {
                            returnedValues = (Object[]) result;
                        } else {
                            returnedValues = new Object[]{result};
                        }
                    }
                }
            }

            if (doDelete) {
                if (version != null && versionFlusher != null) {
                    Query query = context.getEntityManager().createQuery(versionedDeleteQuery);
                    idFlusher.flushQuery(context, null, query, view, id);
                    query.setParameter(EntityViewUpdaterImpl.VERSION_PARAM_NAME, version);
                    int updated = query.executeUpdate();
                    if (updated != 1) {
                        throw new OptimisticLockException(entity, view);
                    }
                } else {
                    Query query = context.getEntityManager().createQuery(deleteQuery);
                    idFlusher.flushQuery(context, null, query, view, id);
                    query.executeUpdate();
                }
            }

            for (PostRemoveDeleter postRemoveDeleter : postRemoveDeleters) {
                postRemoveDeleter.execute(context);
            }

            for (int i = 0; i < unmappedPostRemoveCascadeDeleters.length; i++) {
                unmappedPostRemoveCascadeDeleters[i].removeById(context, returnedValues[i]);
            }

            if (cascadeMappedDeletes) {
                int valueIndex = unmappedPostRemoveCascadeDeleters.length;
                for (int i = 0; i < flushers.length; i++) {
                    final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                    if (flusher != null && flusher.requiresDeleteCascadeAfterRemove() && flusher.getElementIdAttributeName() != null) {
                        flusher.remove(context, returnedValues[valueIndex++]);
                    }
                }
            }
        }
    }

    @Override
    public List<PostRemoveDeleter> removeByOwnerId(UpdateContext context, Object id) {
        // A composite flusher needs to be wrapped in a subview or collection flusher
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean requiresDeleteCascadeAfterRemove() {
        // A composite flusher needs to be wrapped in a subview or collection flusher
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isViewOnlyDeleteCascaded() {
        // A composite flusher needs to be wrapped in a subview or collection flusher
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPassThrough() {
        // A composite flusher needs to be wrapped in a subview or collection flusher
        throw new UnsupportedOperationException();
    }

    @Override
    public String getElementIdAttributeName() {
        // A composite flusher needs to be wrapped in a subview or collection flusher
        throw new UnsupportedOperationException();
    }

    @Override
    public AttributeAccessor getViewAttributeAccessor() {
        return null;
    }

    @Override
    public boolean isOptimisticLockProtected() {
        return optimisticLockProtected;
    }

    @Override
    public boolean requiresFlushAfterPersist(Object value) {
        return false;
    }

    @Override
    public DirtyAttributeFlusher<CompositeAttributeFlusher, Object, Object> getDirtyFlusher(UpdateContext context, Object view, Object initial, Object current) {
        return this;
    }

    @Override
    public <X> DirtyChecker<X>[] getNestedCheckers(Object current) {
        return (DirtyChecker<X>[]) flushers;
    }

    @Override
    public DirtyKind getDirtyKind(Object initial, Object current) {
        if (current == null) {
            if (initial == null) {
                return DirtyKind.NONE;
            }
            return DirtyKind.UPDATED;
        }
        if (initial == null) {
            return DirtyKind.UPDATED;
        }

        DirtyStateTrackable currentObject = (DirtyStateTrackable) current;
        DirtyStateTrackable initialObject = (DirtyStateTrackable) initial;
        // Skip further checks if we detect identity change
        if (initialObject != currentObject && !initialObject.equals(currentObject)) {
            return DirtyKind.UPDATED;
        }
        if (!currentObject.$$_isDirty()) {
            return DirtyKind.NONE;
        }
        long dirty = currentObject.$$_getSimpleDirty();
        Object[] initialState = initialObject.$$_getInitialState();
        Object[] dirtyState = currentObject.$$_getMutableState();

        for (int i = 0; i < initialState.length; i++) {
            long mask = 1L << i;
            if ((dirty & mask) != 0) {
                if (flushers[i].getDirtyKind(initialState[i], dirtyState[i]) != DirtyKind.NONE) {
                    return DirtyKind.MUTATED;
                }
            }
        }
        return DirtyKind.NONE;
    }

    @SuppressWarnings("unchecked")
    public <T extends DirtyAttributeFlusher<T, E, V>, E, V> DirtyAttributeFlusher<T, E, V> getNestedDirtyFlusher(UpdateContext context, MutableStateTrackable updatableProxy) {
        // When we persist, always flush all attributes
        boolean shouldPersist = updatableProxy.$$_isNew();
        if (context.isForceFull() || flushMode == FlushMode.FULL || shouldPersist || !(updatableProxy instanceof DirtyStateTrackable)) {
            return (DirtyAttributeFlusher<T, E, V>) this;
        }

        Object[] initialState = ((DirtyStateTrackable) updatableProxy).$$_getInitialState();
        Object[] originalDirtyState = updatableProxy.$$_getMutableState();
        @SuppressWarnings("unchecked")
        DirtyAttributeFlusher[] flushers = new DirtyAttributeFlusher[originalDirtyState.length];
        // Copy flushers to the target candidate flushers
        if (!updatableProxy.$$_copyDirty(this.flushers, flushers)) {
            // If the dirty detection says nothing is dirty, we don't need to do anything
            return null;
        }

        boolean first = true;
        for (int i = 0; i < originalDirtyState.length; i++) {
            if (flushers[i] != null) {
                Object newState = originalDirtyState[i];
                DirtyAttributeFlusher flusher = flushers[i].getDirtyFlusher(context, updatableProxy, initialState[i], newState);

                if (flusher == null) {
                    flushers[i] = null;
                } else {
                    flushers[i] = flusher;

                    if (first) {
                        first = false;
                    }
                }
            }
        }

        // If nothing is dirty, we don't have to do anything
        if (first) {
            return null;
        }

        // If we know something is dirty, we copy over the pass through flushers too
        if (hasPassThroughFlushers) {
            for (int i = originalDirtyState.length; i < flushers.length; i++) {
                DirtyAttributeFlusher flusher = this.flushers[i];

                if (flusher != null && flusher.isPassThrough()) {
                    flushers[i] = flusher;
                }
            }
        }

        return (DirtyAttributeFlusher<T, E, V>) new CompositeAttributeFlusher(this, flushers, updatableProxy, false);
    }

}
