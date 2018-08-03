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

import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.accessor.InitialValueAttributeAccessor;
import com.blazebit.persistence.view.impl.change.PluralDirtyChecker;
import com.blazebit.persistence.view.impl.collection.CollectionRemoveListener;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.proxy.MutableStateTrackable;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.spi.type.BasicUserType;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractPluralAttributeFlusher<X extends AbstractPluralAttributeFlusher<X, A, R, E, V>, A, R, E, V> extends AttributeFetchGraphNode<X> implements DirtyAttributeFlusher<X, E, V>, PluralDirtyChecker<V, E> {

    protected final Class<?> ownerEntityClass;
    protected final String ownerIdAttributeName;
    protected final FlushStrategy flushStrategy;
    protected final AttributeAccessor entityAttributeMapper;
    protected final InitialValueAttributeAccessor viewAttributeAccessor;
    protected final boolean optimisticLockProtected;
    protected final boolean collectionUpdatable;
    protected final boolean viewOnlyDeleteCascaded;
    protected final boolean jpaProviderDeletesCollection;
    protected final CollectionRemoveListener cascadeDeleteListener;
    protected final CollectionRemoveListener removeListener;
    protected final TypeDescriptor elementDescriptor;
    protected final EqualityChecker elementEqualityChecker;
    protected final BasicDirtyChecker<Object> elementDirtyChecker;
    protected final PluralFlushOperation flushOperation;
    protected final List<? extends A> collectionActions;
    protected final List<CollectionElementAttributeFlusher<E, V>> elementFlushers;

    @SuppressWarnings("unchecked")
    public AbstractPluralAttributeFlusher(String attributeName, String mapping, boolean fetch, Class<?> ownerEntityClass, String ownerIdAttributeName, FlushStrategy flushStrategy, AttributeAccessor entityAttributeMapper, InitialValueAttributeAccessor viewAttributeAccessor, boolean optimisticLockProtected, boolean collectionUpdatable,
                                          boolean viewOnlyDeleteCascaded, boolean jpaProviderDeletesCollection, CollectionRemoveListener cascadeDeleteListener, CollectionRemoveListener removeListener, TypeDescriptor elementDescriptor) {
        super(attributeName, mapping, fetch, elementDescriptor.getViewToEntityMapper() == null ? null : elementDescriptor.getViewToEntityMapper().getFullGraphNode());
        this.ownerEntityClass = ownerEntityClass;
        this.ownerIdAttributeName = ownerIdAttributeName;
        this.flushStrategy = flushStrategy;
        this.entityAttributeMapper = entityAttributeMapper;
        this.viewAttributeAccessor = viewAttributeAccessor;
        this.optimisticLockProtected = optimisticLockProtected;
        this.collectionUpdatable = collectionUpdatable;
        this.viewOnlyDeleteCascaded = viewOnlyDeleteCascaded;
        this.jpaProviderDeletesCollection = jpaProviderDeletesCollection;
        this.cascadeDeleteListener = cascadeDeleteListener;
        this.removeListener = removeListener;
        this.elementDescriptor = elementDescriptor;
        if (elementDescriptor.isSubview() || elementDescriptor.isJpaEntity()) {
            this.elementDirtyChecker = null;
        } else {
            this.elementDirtyChecker = new BasicDirtyChecker<>(elementDescriptor);
        }
        if (elementDescriptor.isSubview()) {
            if (elementDescriptor.isIdentifiable()) {
                elementEqualityChecker = new EntityIdWithViewIdEqualityChecker(elementDescriptor.getViewToEntityMapper());
            } else {
                elementEqualityChecker = new EntityWithViewEqualityChecker(elementDescriptor.getViewToEntityMapper());
            }
        } else {
            if (elementDescriptor.isIdentifiable()) {
                elementEqualityChecker = new IdentityEqualityChecker(elementDescriptor.getBasicUserType());
            } else {
                elementEqualityChecker = new DeepEqualityChecker(elementDescriptor.getBasicUserType());
            }
        }
        this.flushOperation = null;
        this.collectionActions = null;
        this.elementFlushers = null;
    }

    protected AbstractPluralAttributeFlusher(AbstractPluralAttributeFlusher<?, ?, ?, ?, ?> original, boolean fetch) {
        this(original, fetch, null, null, null);
    }

    protected AbstractPluralAttributeFlusher(AbstractPluralAttributeFlusher<?, ?, ?, ?, ?> original, boolean fetch, PluralFlushOperation flushOperation, List<? extends A> collectionActions, List<CollectionElementAttributeFlusher<E, V>> elementFlushers) {
        super(original.attributeName, original.mapping, fetch, elementFlushers == null ? original.nestedGraphNode : computeElementFetchGraphNode(elementFlushers));
        this.ownerEntityClass = original.ownerEntityClass;
        this.ownerIdAttributeName = original.ownerIdAttributeName;
        this.flushStrategy = original.flushStrategy;
        this.entityAttributeMapper = original.entityAttributeMapper;
        this.viewAttributeAccessor = original.viewAttributeAccessor;
        this.optimisticLockProtected = original.optimisticLockProtected;
        this.collectionUpdatable = original.collectionUpdatable;
        this.viewOnlyDeleteCascaded = original.viewOnlyDeleteCascaded;
        this.jpaProviderDeletesCollection = original.jpaProviderDeletesCollection;
        this.cascadeDeleteListener = original.cascadeDeleteListener;
        this.removeListener = original.removeListener;
        this.elementDescriptor = original.elementDescriptor;
        this.elementDirtyChecker = original.elementDirtyChecker;
        this.elementEqualityChecker = original.elementEqualityChecker;
        this.flushOperation = flushOperation;
        this.collectionActions = collectionActions;
        this.elementFlushers = elementFlushers;
    }

    private static <E, V> FetchGraphNode<?> computeElementFetchGraphNode(List<CollectionElementAttributeFlusher<E, V>> elementFlushers) {
        if (elementFlushers == null || elementFlushers.isEmpty()) {
            return null;
        }
        CollectionElementAttributeFlusher<E, V> elementAttributeFlusher = elementFlushers.get(0);
        if (elementFlushers.size() == 1) {
            return elementAttributeFlusher;
        }
        return elementAttributeFlusher.mergeWith(elementFlushers);
    }

    @Override
    public void appendUpdateQueryFragment(UpdateContext context, StringBuilder sb, String mappingPrefix, String parameterPrefix, String separator) {
    }

    @Override
    public boolean supportsQueryFlush() {
        // TODO: Maybe also when collectionUpdatable is false?
        return mapping == null || flushStrategy != FlushStrategy.ENTITY && !fetch && flushOperation == PluralFlushOperation.ELEMENT_ONLY;
    }

    @Override
    public boolean loadForEntityFlush() {
        return mapping != null;
    }

    @Override
    public void flushQuery(UpdateContext context, String parameterPrefix, Query query, Object view, V value, UnmappedOwnerAwareDeleter ownerAwareDeleter) {
        if (!supportsQueryFlush()) {
            throw new UnsupportedOperationException("Query flush not supported for configuration!");
        }

        for (CollectionElementAttributeFlusher<E, V> elementFlusher : elementFlushers) {
            elementFlusher.flushQuery(context, null, null, view, value, ownerAwareDeleter);
        }
    }

    protected final V getEntityAttributeValue(E entity) {
        if (entityAttributeMapper == null) {
            return null;
        }
        V value = (V) entityAttributeMapper.getValue(entity);
        if (value == null) {
            value = createJpaCollection();
            entityAttributeMapper.setValue(entity, value);
        }

        return value;
    }

    protected abstract V createJpaCollection();

    @SuppressWarnings("unchecked")
    protected void invokeFlushOperation(UpdateContext context, Object view, E entity, V value) {
        switch (flushOperation) {
            case COLLECTION_REPLAY_AND_ELEMENT:
                if (flushStrategy == FlushStrategy.ENTITY) {
                    for (CollectionElementAttributeFlusher<E, V> elementFlusher : elementFlushers) {
                        elementFlusher.flushEntity(context, entity, view, value, null);
                    }
                } else {
                    for (CollectionElementAttributeFlusher<E, V> elementFlusher : elementFlushers) {
                        elementFlusher.flushQuery(context, null, null, view, value, null);
                    }
                }
                invokeCollectionAction(context, getEntityAttributeValue(entity), collectionActions);
                return;
            case COLLECTION_REPLAY_ONLY:
                invokeCollectionAction(context, getEntityAttributeValue(entity), collectionActions);
                return;
            case COLLECTION_REPLACE_AND_ELEMENT:
                if (flushStrategy == FlushStrategy.ENTITY) {
                    for (CollectionElementAttributeFlusher<E, V> elementFlusher : elementFlushers) {
                        elementFlusher.flushEntity(context, entity, view, value, null);
                    }
                } else {
                    for (CollectionElementAttributeFlusher<E, V> elementFlusher : elementFlushers) {
                        elementFlusher.flushQuery(context, null, null, view, value, null);
                    }
                }
                replaceCollection(context, entity, value);
                return;
            case COLLECTION_REPLACE_ONLY:
                replaceCollection(context, entity, value);
                return;
            case ELEMENT_ONLY:
                mergeCollectionElements(context, view, entity, value);
                return;
            default:
                throw new UnsupportedOperationException("Unsupported flush operation: " + flushOperation);
        }
    }

    protected abstract void invokeCollectionAction(UpdateContext context, V targetCollection, List<? extends A> collectionActions);

    protected abstract V replaceWithRecordingCollection(UpdateContext context, Object view, V value, List<? extends A> actions);

    @Override
    public boolean isPassThrough() {
        return !collectionUpdatable && !elementDescriptor.shouldFlushMutations();
    }

    @Override
    public String getElementIdAttributeName() {
        return null;
    }

    @Override
    public AttributeAccessor getViewAttributeAccessor() {
        return viewAttributeAccessor;
    }

    @Override
    public boolean isOptimisticLockProtected() {
        return optimisticLockProtected;
    }

    @Override
    public boolean requiresFlushAfterPersist(V value) {
        return false;
    }

    protected final <X> X persistOrMerge(EntityManager em, X object) {
        return persistOrMerge(em, object, elementDescriptor);
    }

    protected final <X> X persistOrMerge(EntityManager em, X object, TypeDescriptor typeDescriptor) {
        if (object != null) {
            if (typeDescriptor.getBasicUserType().shouldPersist(object)) {
                if (typeDescriptor.shouldJpaPersist()) {
                    em.persist(object);
                }
            } else if (typeDescriptor.shouldJpaMerge()) {
                return em.merge(object);
            }
        }

        return object;
    }

    protected final void persistIfNeeded(EntityManager em, Object object, BasicUserType<Object> basicUserType) {
        if (object != null) {
            if (basicUserType.shouldPersist(object)) {
                em.persist(object);
            }
        }
    }

    protected abstract boolean mergeCollectionElements(UpdateContext context, Object view, E entity, V value);

    protected abstract void replaceCollection(UpdateContext context, E entity, V value);

    @SuppressWarnings("unchecked")
    protected final DirtyAttributeFlusher<X, E, V> getElementOnlyFlusher(UpdateContext context, V current) {
        List<CollectionElementAttributeFlusher<E, V>> elementFlushers = getElementFlushers(context, current);
        // A "null" element flusher list is given when a fetch and compare is more appropriate
        if (elementFlushers == null) {
            return this;
        }

        if (elementFlushers.isEmpty()) {
            return null;
        }

        if (elementDescriptor.shouldJpaPersist()) {
            if (elementFlushers.get(0) instanceof PersistCollectionElementAttributeFlusher<?, ?>) {
                // No need to fetch the relation when we only have persist flushers to invoke
                return partialFlusher(false, PluralFlushOperation.ELEMENT_ONLY, Collections.EMPTY_LIST, elementFlushers);
            }
        }

        // We fetch here, because there is a high probability that elements we update were previously contained in the collection
        // Except when we use a query strategy here, we'd rather use update queries to do the update
        if (flushStrategy == FlushStrategy.ENTITY) {
            return partialFlusher(true, PluralFlushOperation.ELEMENT_ONLY, Collections.EMPTY_LIST, elementFlushers);
        } else {
            return partialFlusher(false, PluralFlushOperation.ELEMENT_ONLY, Collections.EMPTY_LIST, elementFlushers);
        }
    }

    @SuppressWarnings("unchecked")
    protected final DirtyAttributeFlusher<X, E, V> getReplaceOrMergeOnlyFlusher(UpdateContext context, V initial, V current) {
        if (collectionEquals(initial, current)) {
            return null;
        } else {
            // No need for loading elements here, we will replace the entire collection and won't update any state of the elements
            return partialFlusher(false, PluralFlushOperation.COLLECTION_REPLACE_ONLY, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        }
    }

    @SuppressWarnings("unchecked")
    protected final DirtyAttributeFlusher<X, E, V> getReplaceOrMergeAndElementFlusher(UpdateContext context, V initial, V current) {
        List<CollectionElementAttributeFlusher<E, V>> elementFlushers = getElementFlushers(context, current);
        // A "null" element flusher list is given when a fetch and compare is more appropriate
        if (elementFlushers == null) {
            return this;
        }

        if (elementFlushers.isEmpty()) {
            if (collectionEquals(initial, current)) {
                return null;
            } else {
                return getReplaceOrMergeOnlyFlusher(context, initial, current);
            }
        }

        // We fetch here, because there is a high probability that elements we update were previously contained in the collection
        return partialFlusher(true, PluralFlushOperation.COLLECTION_REPLACE_AND_ELEMENT, Collections.EMPTY_LIST, elementFlushers);
    }

    @SuppressWarnings("unchecked")
    protected final DirtyAttributeFlusher<X, E, V> getReplayOnlyFlusher(UpdateContext context, V initial, V current, List<? extends A> collectionActions) {
        if (collectionActions.isEmpty() && collectionEquals(initial, current)) {
            return null;
        } else {
            // Merging always requires figuring out the diff between collections
            // Maybe at some point we could issue a SQL MERGE statement to implement this, but for now we need to fetch
            return partialFlusher(true, PluralFlushOperation.COLLECTION_REPLAY_ONLY, collectionActions, Collections.<CollectionElementAttributeFlusher<E, V>>emptyList());
        }
    }

    @SuppressWarnings("unchecked")
    protected final DirtyAttributeFlusher<X, E, V> getReplayAndElementFlusher(UpdateContext context, V initial, V current, List<? extends A> collectionActions, List<CollectionElementAttributeFlusher<E, V>> elementFlushers) {
        if (elementFlushers.isEmpty()) {
            // TODO: is collection equals really needed here?
            if (collectionActions.isEmpty() && collectionEquals(initial, current)) {
                return null;
            } else {
                return getReplayOnlyFlusher(context, initial, current, collectionActions);
            }
        }

        // We fetch here, because there is a high probability that elements we update were previously contained in the collection
        // and also because we need a fetched collection to actually compute the diff for a proper merge
        return partialFlusher(true, PluralFlushOperation.COLLECTION_REPLAY_AND_ELEMENT, collectionActions, elementFlushers);
    }

    protected abstract List<CollectionElementAttributeFlusher<E, V>> getElementFlushers(UpdateContext context, V current);

    protected final boolean determineElementFlushers(UpdateContext context, TypeDescriptor typeDescriptor, List<CollectionElementAttributeFlusher<E, V>> elementFlushers, Iterable<?> current) {
        if (typeDescriptor.shouldFlushMutations()) {
            if (typeDescriptor.isSubview()) {
                final ViewToEntityMapper mapper = typeDescriptor.getViewToEntityMapper();
                if (typeDescriptor.isIdentifiable()) {
                    for (Object o : current) {
                        if (o instanceof MutableStateTrackable) {
                            MutableStateTrackable element = (MutableStateTrackable) o;
                            @SuppressWarnings("unchecked")
                            DirtyAttributeFlusher<?, E, V> flusher = (DirtyAttributeFlusher<?, E, V>) (DirtyAttributeFlusher) mapper.getNestedDirtyFlusher(context, element, (DirtyAttributeFlusher) null);
                            if (flusher != null) {
                                elementFlushers.add(new UpdateCollectionElementAttributeFlusher<E, V>(flusher, element, optimisticLockProtected, mapper));
                            }
                        }
                    }
                } else {
                    for (Object o : current) {
                        if (o instanceof MutableStateTrackable) {
                            MutableStateTrackable element = (MutableStateTrackable) o;
                            @SuppressWarnings("unchecked")
                            DirtyAttributeFlusher<?, E, V> flusher = (DirtyAttributeFlusher<?, E, V>) (DirtyAttributeFlusher) mapper.getNestedDirtyFlusher(context, element, (DirtyAttributeFlusher) null);
                            if (flusher != null) {
                                // We can't merge flat view elements separately so we need to replace the element in the collection
                                // This is signalled by returning null
                                return true;
                            }
                        }
                    }

                }
            } else if (typeDescriptor.isJpaEntity()) {
                for (Object element : current) {
                    if (typeDescriptor.getBasicUserType().shouldPersist(element) && typeDescriptor.shouldJpaPersist()) {
                        elementFlushers.add(new PersistCollectionElementAttributeFlusher<E, V>(element, optimisticLockProtected));
                    } else if (typeDescriptor.shouldJpaMerge()) {
                        // We can't replace the original object efficiently in the backing collection which is required because em.merge returns a new object
                        // And since merges need the current state, we rather fetch the collection and merge/persist only during the actual flushing
                        // This is signalled by returning null
                        return true;
                    }
                }
            } else if (typeDescriptor.getBasicUserType().supportsDirtyChecking()) {
                for (Object element : current) {
                    String[] dirtyProperties = typeDescriptor.getBasicUserType().getDirtyProperties(element);
                    if (dirtyProperties != null) {
                        // We can't merge basic elements separately so we need to replace the element in the collection
                        // This is signalled by returning null
                        return true;
                    }
                }
            } else {
                throw new IllegalArgumentException("Element flushers for non-identifiable type not determinable: " + typeDescriptor);
            }
        }

        return false;
    }

    protected abstract AbstractPluralAttributeFlusher<X, A, R, E, V> partialFlusher(boolean fetch, PluralFlushOperation operation, List<? extends A> collectionActions, List<CollectionElementAttributeFlusher<E, V>> elementFlushers);

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    protected static enum PluralFlushOperation {
        ELEMENT_ONLY,
        COLLECTION_REPLACE_ONLY,
        COLLECTION_REPLACE_AND_ELEMENT,
        COLLECTION_REPLAY_ONLY,
        COLLECTION_REPLAY_AND_ELEMENT;
    }

    protected abstract boolean collectionEquals(V initial, V current);

    protected abstract DirtyAttributeFlusher<X, E, V> getDirtyFlusherForRecordingCollection(UpdateContext context, V initial, R collection);

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    protected static interface EqualityChecker {
        public boolean isEqual(UpdateContext context, Object object1, Object object2);
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    protected static final class IdentityEqualityChecker implements EqualityChecker {

        private final BasicUserType<Object> type;

        public IdentityEqualityChecker(BasicUserType<Object> type) {
            this.type = type;
        }

        @Override
        public boolean isEqual(UpdateContext context, Object object1, Object object2) {
            if (object1 == null) {
                return object2 == null;
            } else if (object2 == null) {
                return false;
            }
            return type.isEqual(object1, object2);
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    protected static final class EqualsEqualityChecker implements EqualityChecker {

        public static final EqualsEqualityChecker INSTANCE = new EqualsEqualityChecker();

        private EqualsEqualityChecker() {
        }

        @Override
        public boolean isEqual(UpdateContext context, Object object1, Object object2) {
            if (object1 == null) {
                return object2 == null;
            } else if (object2 == null) {
                return false;
            }
            return object1.equals(object2);
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    protected static final class DeepEqualityChecker implements EqualityChecker {

        private final BasicUserType<Object> type;

        public DeepEqualityChecker(BasicUserType<Object> type) {
            this.type = type;
        }

        @Override
        public boolean isEqual(UpdateContext context, Object object1, Object object2) {
            if (object1 == null) {
                return object2 == null;
            } else if (object2 == null) {
                return false;
            }
            return type.isDeepEqual(object1, object2);
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    protected static final class EntityWithViewEqualityChecker implements EqualityChecker {

        private final ViewToEntityMapper mapper;

        public EntityWithViewEqualityChecker(ViewToEntityMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public boolean isEqual(UpdateContext context, Object entity, Object view) {
            if (entity == null) {
                return view == null;
            } else if (view == null) {
                return false;
            }
            return entity.equals(mapper.applyToEntity(context, null, view));
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    protected static final class EntityIdWithViewIdEqualityChecker implements EqualityChecker {

        private final ViewToEntityMapper mapper;
        private final ViewToEntityMapper idMapper;

        public EntityIdWithViewIdEqualityChecker(ViewToEntityMapper mapper) {
            this.mapper = mapper;
            DirtyAttributeFlusher<?, Object, Object> idFlusher = ((CompositeAttributeFlusher) mapper.getFullGraphNode()).getIdFlusher();
            if (idFlusher instanceof EmbeddableAttributeFlusher<?, ?>) {
                this.idMapper = ((EmbeddableAttributeFlusher) idFlusher).getViewToEntityMapper();
            } else {
                this.idMapper = null;
            }
        }

        @Override
        public boolean isEqual(UpdateContext context, Object entity, Object view) {
            if (entity == null) {
                return view == null;
            } else if (view == null) {
                return false;
            }
            Object idValue = mapper.getViewIdAccessor().getValue(view);
            if (idMapper == null) {
                return mapper.getEntityIdAccessor().getValue(entity).equals(idValue);
            } else {
                return mapper.getEntityIdAccessor().getValue(entity).equals(idMapper.applyToEntity(context, null, idValue));
            }
        }
    }

}
