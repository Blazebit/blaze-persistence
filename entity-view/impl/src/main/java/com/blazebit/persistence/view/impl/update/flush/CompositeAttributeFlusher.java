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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.change.DirtyChecker;
import com.blazebit.persistence.view.impl.entity.EntityLoader;
import com.blazebit.persistence.view.impl.entity.EntityTupleizer;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.entity.FlusherBasedEntityLoader;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.proxy.DirtyStateTrackable;
import com.blazebit.persistence.view.impl.proxy.MutableStateTrackable;

import javax.persistence.Query;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CompositeAttributeFlusher extends CompositeAttributeFetchGraphNode<CompositeAttributeFlusher, DirtyAttributeFlusher<?, Object, Object>> implements DirtyAttributeFlusher<CompositeAttributeFlusher, Object, Object> {

    private static final int FEATURE_SUPPORTS_QUERY_FLUSH = 0;
    private static final int FEATURE_HAS_PASS_THROUGH_FLUSHER = 1;
    private static final int FEATURE_IS_ANY_OPTIMISTIC_LOCK_PROTECTED = 2;

    private final Class<?> entityClass;
    private final boolean persistable;
    private final javax.persistence.metamodel.SingularAttribute<?, ?> jpaIdAttribute;
    private final ViewToEntityMapper viewIdMapper;
    private final AttributeAccessor viewIdAccessor;
    private final EntityTupleizer tupleizer;
    private final ObjectBuilder<Object> idViewBuilder;
    private final DirtyAttributeFlusher<?, Object, Object> idFlusher;
    private final DirtyAttributeFlusher<?, Object, Object> versionFlusher;
    private final FlushMode flushMode;
    private final FlushStrategy flushStrategy;
    private final EntityLoader entityLoader;
    private final boolean supportsQueryFlush;
    private final boolean hasPassThroughFlushers;
    private final boolean optimisticLockProtected;

    private final Object element;

    @SuppressWarnings("unchecked")
    public CompositeAttributeFlusher(Class<?> viewType, Class<?> entityClass, boolean persistable, SingularAttribute<?, ?> jpaIdAttribute,
                                     ViewToEntityMapper viewIdMapper, AttributeAccessor viewIdAccessor, EntityTupleizer tupleizer, ObjectBuilder<Object> idViewBuilder, DirtyAttributeFlusher<?, Object, Object> idFlusher,
                                     DirtyAttributeFlusher<?, Object, Object> versionFlusher, DirtyAttributeFlusher[] flushers, FlushMode flushMode, FlushStrategy flushStrategy) {
        super(viewType, flushers, null);
        this.entityClass = entityClass;
        this.persistable = persistable;
        this.jpaIdAttribute = jpaIdAttribute;
        this.viewIdMapper = viewIdMapper;
        this.viewIdAccessor = viewIdAccessor;
        this.tupleizer = tupleizer;
        this.idViewBuilder = idViewBuilder;
        this.idFlusher = idFlusher;
        this.versionFlusher = versionFlusher;
        this.flushMode = flushMode;
        this.flushStrategy = flushStrategy;
        this.entityLoader = new FlusherBasedEntityLoader(entityClass, jpaIdAttribute, viewIdMapper, flushers);
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
        this.jpaIdAttribute = original.jpaIdAttribute;
        this.viewIdMapper = original.viewIdMapper;
        this.viewIdAccessor = original.viewIdAccessor;
        this.tupleizer = original.tupleizer;
        this.idViewBuilder = original.idViewBuilder;
        this.idFlusher = original.idFlusher;
        this.versionFlusher = original.versionFlusher;
        this.flushMode = original.flushMode;
        this.flushStrategy = original.flushStrategy;
        this.entityLoader = new FlusherBasedEntityLoader(entityClass, jpaIdAttribute, viewIdMapper, flushers);
        this.supportsQueryFlush = supportsQueryFlush(flushers);
        this.hasPassThroughFlushers = original.hasPassThroughFlushers;
        this.optimisticLockProtected = original.optimisticLockProtected;
        this.element = element;
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
            flushEntity(context, null, value, value);
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
                flushers[i].flushQuery(context, parameterPrefix, query, value, flushers[i].getViewAttributeAccessor().getValue(context, value));
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean flushEntity(UpdateContext context, Object entity, Object view, Object value) {
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
        try {
            Object id = updatableProxy.$$_getId();

            if (doPersist) {
                // In case of nested attributes, the entity instance we get is the container of the attribute
                if (entity == null || !entityClass.isInstance(entity)) {
                    entity = entityLoader.toEntity(context, null);
                }

                if (id != null) {
                    idFlusher.flushEntity(context, entity, updatableProxy, id);
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
                                wasDirty |= flusher.flushEntity(context, entity, value, state[i]);
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < state.length; i++) {
                        final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                        if (flusher != null) {
                            initialState[i] = flusher.cloneDeep(value, initialState[i], state[i]);
                            wasDirty |= flusher.flushEntity(context, entity, value, state[i]);
                        }
                    }
                }
            } else {
                if (doPersist) {
                    for (int i = 0; i < state.length; i++) {
                        final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                        if (flusher != null) {
                            wasDirty |= flusher.flushEntity(context, entity, value, state[i]);
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
                                wasDirty |= flusher.flushEntity(context, entity, value, state[i]);
                            }
                        }
                    }
                }
            }

            // Pass through flushers
            for (int i = state.length; i < flushers.length; i++) {
                final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                if (flushers[i] != null) {
                    wasDirty |= flusher.flushEntity(context, entity, value, flusher.getViewAttributeAccessor().getValue(context, value));
                }
            }
            if (versionFlusher != null && wasDirty) {
                context.getInitialStateResetter().addVersionedView(updatableProxy, updatableProxy.$$_getVersion());
                versionFlusher.flushEntity(context, entity, value, updatableProxy.$$_getVersion());
            }
            if (doPersist) {
                // If the class of the object is an entity, we persist the object
                context.getEntityManager().persist(entity);
                id = entityLoader.getEntityId(context, entity);
                if (tupleizer != null) {
                    Object[] tuple = tupleizer.tupleize(id);
                    id = idViewBuilder.build(tuple);
                }
                viewIdAccessor.setValue(context, updatableProxy, id);
            }
            return wasDirty;
        } finally {
            if (shouldPersist) {
                context.getInitialStateResetter().addPersistedView(updatableProxy);

                if (deferredFlushers != null) {
                    Object[] state = updatableProxy.$$_getMutableState();
                    for (int i = 0; i < deferredFlushers.size(); i++) {
                        final int index = deferredFlushers.get(i);
                        final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[index];
                        flusher.flushEntity(context, entity, value, state[index]);
                    }
                }
            }
        }
    }

    @Override
    public void remove(UpdateContext context, Object entity, Object view, Object value) {
        MutableStateTrackable updatableProxy = (MutableStateTrackable) value;

        // Only remove object that are
        // 1. Persistable i.e. of an entity type that can be removed
        // 2. Have no parent
        // 3. Haven't been removed yet
        // 4. Aren't new i.e. only existing objects, no need to delete object that haven't been persisted yet
        if (persistable && !updatableProxy.$$_hasParent() && context.addRemovedObject(value) && !updatableProxy.$$_isNew()) {
            Object[] state = updatableProxy.$$_getMutableState();
            for (int i = 0; i < state.length; i++) {
                final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                if (flusher != null) {
                    flusher.remove(context, entity, value, state[i]);
                }
            }

            Object id = updatableProxy.$$_getId();
            entity = entityLoader.toEntity(context, id);
            context.getEntityManager().remove(entity);
        }
    }

    @Override
    public boolean isPassThrough() {
        // TODO: Not sure if a composite can ever be a pass through flusher
        return false;
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
        if (context.isForceFull() || flushMode == FlushMode.FULL || !(updatableProxy instanceof DirtyStateTrackable)) {
            return (DirtyAttributeFlusher<T, E, V>) this;
        }

        boolean shouldPersist = updatableProxy.$$_isNew();
        Object[] initialState = ((DirtyStateTrackable) updatableProxy).$$_getInitialState();
        Object[] originalDirtyState = updatableProxy.$$_getMutableState();
        @SuppressWarnings("unchecked")
        DirtyAttributeFlusher[] flushers = new DirtyAttributeFlusher[originalDirtyState.length];
        // Copy flushers to the target candidate flushers
        if (!updatableProxy.$$_copyDirty(this.flushers, flushers) && !shouldPersist) {
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
        if (first && !shouldPersist) {
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

        return (DirtyAttributeFlusher<T, E, V>) new CompositeAttributeFlusher(this, flushers, updatableProxy, shouldPersist);
    }

}
