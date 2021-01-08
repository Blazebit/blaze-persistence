/*
 * Copyright 2014 - 2021 Blazebit.
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
import com.blazebit.persistence.view.spi.type.MutableStateTrackable;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.UpdateQueryFactory;
import com.blazebit.persistence.view.spi.type.BasicUserType;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
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
    protected final String ownerMapping;
    protected final DirtyAttributeFlusher<?, Object, Object> ownerIdFlusher;
    protected final boolean supportsCollectionDml;
    protected final String ownerIdWhereFragment;
    protected final String[] ownerIdBindFragments;
    protected final DirtyAttributeFlusher<?, Object, Object> elementFlusher;
    protected final FlushStrategy flushStrategy;
    protected final AttributeAccessor entityAttributeAccessor;
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
    // For now, we always replace the collections contents with the ones from the reference view, but at some point we might support an "additive" mode
    protected final boolean replaceWithReferenceContents = true;

    @SuppressWarnings("unchecked")
    public AbstractPluralAttributeFlusher(String attributeName, String mapping, boolean fetch, Class<?> ownerEntityClass, String ownerIdAttributeName, String ownerMapping, DirtyAttributeFlusher<?, ?, ?> ownerIdFlusher, DirtyAttributeFlusher<?, ?, ?> elementFlusher, boolean supportsCollectionDml, FlushStrategy flushStrategy, AttributeAccessor entityAttributeAccessor,
                                          InitialValueAttributeAccessor viewAttributeAccessor, boolean optimisticLockProtected, boolean collectionUpdatable, boolean viewOnlyDeleteCascaded, boolean jpaProviderDeletesCollection, CollectionRemoveListener cascadeDeleteListener, CollectionRemoveListener removeListener, TypeDescriptor elementDescriptor) {
        super(attributeName, mapping, fetch, null);
        this.ownerEntityClass = ownerEntityClass;
        this.ownerIdAttributeName = ownerIdAttributeName;
        this.ownerMapping = ownerMapping;
        this.ownerIdFlusher = (DirtyAttributeFlusher<?, Object, Object>) ownerIdFlusher;
        this.supportsCollectionDml = supportsCollectionDml;
        this.elementFlusher = (DirtyAttributeFlusher<?, Object, Object>) elementFlusher;
        if (ownerIdFlusher == null) {
            this.ownerIdWhereFragment = null;
            this.ownerIdBindFragments = null;
        } else {
            StringBuilder sb = new StringBuilder();
            ownerIdFlusher.appendUpdateQueryFragment(null, sb, null, null, " AND ");
            this.ownerIdWhereFragment = sb.toString();
            sb.setLength(0);
            ownerIdFlusher.appendUpdateQueryFragment(null, sb, null, null, ",");
            String[] fragments = sb.toString().split("\\s*(=|,)\\s*");
            for (int i = 1; i < fragments.length; i += 2) {
                fragments[i] = "FUNCTION('TREAT_INTEGER', " + fragments[i] + ")";
            }
            this.ownerIdBindFragments = fragments;
        }
        this.flushStrategy = flushStrategy;
        this.entityAttributeAccessor = entityAttributeAccessor;
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
        super(original.attributeName, original.mapping, fetch, elementFlushers == null ? original.getNestedGraphNode() : computeElementFetchGraphNode(elementFlushers));
        this.ownerEntityClass = original.ownerEntityClass;
        this.ownerIdAttributeName = original.ownerIdAttributeName;
        this.ownerMapping = original.ownerMapping;
        this.ownerIdFlusher = original.ownerIdFlusher;
        this.supportsCollectionDml = original.supportsCollectionDml;
        this.ownerIdWhereFragment = original.ownerIdWhereFragment;
        this.ownerIdBindFragments = original.ownerIdBindFragments;
        this.elementFlusher = original.elementFlusher;
        this.flushStrategy = original.flushStrategy;
        this.entityAttributeAccessor = original.entityAttributeAccessor;
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

    @Override
    protected FetchGraphNode<?> getNestedGraphNode() {
        FetchGraphNode<?> nestedGraphNode = super.getNestedGraphNode();
        return nestedGraphNode == null && elementDescriptor.getViewToEntityMapper() != null && elementFlushers == null ? elementDescriptor.getViewToEntityMapper().getFullGraphNode() : nestedGraphNode;
    }

    private static <E, V> FetchGraphNode<?> computeElementFetchGraphNode(List<CollectionElementAttributeFlusher<E, V>> elementFlushers) {
        if (elementFlushers == null || elementFlushers.isEmpty()) {
            return null;
        }
        CollectionElementAttributeFlusher<E, V> elementAttributeFlusher = null;
        List<CollectionElementAttributeFlusher<E, V>> filteredElementFlushers = null;
        for (int i = 0; i < elementFlushers.size(); i++) {
            CollectionElementAttributeFlusher<E, V> flusher = elementFlushers.get(i);
            if (flusher instanceof MergeCollectionElementAttributeFlusher<?, ?>) {
                if (filteredElementFlushers == null) {
                    filteredElementFlushers = new ArrayList<>();
                    for (int j = 0; j < i; j++) {
                        filteredElementFlushers.add(elementFlushers.get(j));
                    }
                }
            } else {
                if (elementAttributeFlusher == null) {
                    elementAttributeFlusher = flusher;
                }
                if (filteredElementFlushers != null) {
                    filteredElementFlushers.add(flusher);
                }
            }
        }
        if (filteredElementFlushers == null) {
            if (elementFlushers.size() == 1) {
                return elementAttributeFlusher;
            }
        } else {
            if (filteredElementFlushers.isEmpty()) {
                return null;
            }
            if (filteredElementFlushers.size() == 1) {
                return elementAttributeFlusher;
            }
            elementFlushers = filteredElementFlushers;
        }
        return elementAttributeFlusher.mergeWith(elementFlushers);
    }

    @Override
    public boolean appendUpdateQueryFragment(UpdateContext context, StringBuilder sb, String mappingPrefix, String parameterPrefix, String separator) {
        return true;
    }

    @Override
    public boolean supportsQueryFlush() {
        // When we have no mapping, this is a correlated attribute, so there are no collection operations, only cascades
        // When dirty checking figured out we only have element flushes, we also don't have collection operations
        return supportsCollectionDml || mapping == null || flushStrategy != FlushStrategy.ENTITY && (!collectionUpdatable || !fetch && flushOperation == PluralFlushOperation.ELEMENT_ONLY);
    }

    @Override
    public boolean loadForEntityFlush() {
        return mapping != null;
    }

    @Override
    public Query flushQuery(UpdateContext context, String parameterPrefix, UpdateQueryFactory queryFactory, Query query, Object ownerView, Object view, V value, UnmappedOwnerAwareDeleter ownerAwareDeleter, DirtyAttributeFlusher<?, ?, ?> ownerFlusher) {
        if (!supportsQueryFlush()) {
            throw new UnsupportedOperationException("Query flush not supported for configuration!");
        }

        for (CollectionElementAttributeFlusher<E, V> elementFlusher : elementFlushers) {
            elementFlusher.flushQuery(context, null, queryFactory, null, ownerView, view, value, ownerAwareDeleter, ownerFlusher);
        }
        return query;
    }

    protected final V getEntityAttributeValue(E entity) {
        if (entityAttributeAccessor == null || entity == null) {
            return null;
        }
        V value = (V) entityAttributeAccessor.getValue(entity);
        if (value == null) {
            value = createJpaCollection();
            entityAttributeAccessor.setValue(entity, value);
        }

        return value;
    }

    protected abstract V createJpaCollection();

    @Override
    public final String getMapping() {
        if (ownerMapping == null) {
            return mapping;
        } else {
            return ownerMapping + "." + mapping;
        }
    }

    @SuppressWarnings("unchecked")
    protected void invokeFlushOperation(UpdateContext context, Object ownerView, Object view, E entity, V value) {
        switch (flushOperation) {
            case COLLECTION_REPLAY_AND_ELEMENT:
                if (flushStrategy == FlushStrategy.ENTITY || context.isForceEntity()) {
                    for (CollectionElementAttributeFlusher<E, V> elementFlusher : elementFlushers) {
                        elementFlusher.flushEntity(context, entity, ownerView, view, value, null);
                    }
                } else {
                    for (CollectionElementAttributeFlusher<E, V> elementFlusher : elementFlushers) {
                        elementFlusher.flushQuery(context, null, null, null, ownerView, view, value, null, null);
                    }
                }
                invokeCollectionAction(context, ownerView, view, getEntityAttributeValue(entity), value, collectionActions);
                return;
            case COLLECTION_REPLAY_ONLY:
                invokeCollectionAction(context, ownerView, view, getEntityAttributeValue(entity), value, collectionActions);
                return;
            case COLLECTION_REPLACE_AND_ELEMENT:
                if (flushStrategy == FlushStrategy.ENTITY || context.isForceEntity()) {
                    for (CollectionElementAttributeFlusher<E, V> elementFlusher : elementFlushers) {
                        elementFlusher.flushEntity(context, entity, ownerView, view, value, null);
                    }
                } else {
                    for (CollectionElementAttributeFlusher<E, V> elementFlusher : elementFlushers) {
                        elementFlusher.flushQuery(context, null, null, null, ownerView, view, value, null, null);
                    }
                }
                replaceCollection(context, ownerView, view, entity, value, flushStrategy);
                return;
            case COLLECTION_REPLACE_ONLY:
                replaceCollection(context, ownerView, view, entity, value, flushStrategy);
                return;
            case ELEMENT_ONLY:
                mergeCollectionElements(context, ownerView, view, entity, value);
                return;
            default:
                throw new UnsupportedOperationException("Unsupported flush operation: " + flushOperation);
        }
    }

    protected abstract void invokeCollectionAction(UpdateContext context, Object ownerView, Object view, V targetCollection, Object value, List<? extends A> collectionActions);

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
    public AttributeAccessor getEntityAttributeAccessor() {
        return entityAttributeAccessor;
    }

    @Override
    public String getLockOwner() {
        return null;
    }

    @Override
    public boolean isOptimisticLockProtected() {
        return optimisticLockProtected;
    }

    @Override
    public boolean requiresFlushAfterPersist(V value) {
        return false;
    }

    @Override
    public boolean requiresDeferredFlush(V value) {
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

    protected abstract boolean mergeCollectionElements(UpdateContext context, Object ownerView, Object view, E entity, V value);

    protected abstract void replaceCollection(UpdateContext context, Object ownerView, Object view, E entity, V value, FlushStrategy flushStrategy);

    protected abstract boolean isIndexed();

    protected abstract void addFlatViewElementFlushActions(UpdateContext context, TypeDescriptor elementDescriptor, List<A> actions, V current);

    protected static Object getViewElement(UpdateContext context, TypeDescriptor typeDescriptor, Object jpaCollectionObject) {
        if (jpaCollectionObject != null && typeDescriptor.isSubview() && typeDescriptor.isIdentifiable()) {
            CompositeAttributeFlusher compositeFlusher = (CompositeAttributeFlusher) typeDescriptor.getViewToEntityMapper().getFullGraphNode();
            Object entityId = compositeFlusher.getIdFlusher().getEntityAttributeAccessor().getValue(jpaCollectionObject);
            return context.getEntityViewManager().getReference(compositeFlusher.getViewTypeClass(), compositeFlusher.createViewIdByEntityId(entityId));
        }
        return jpaCollectionObject;
    }

    protected static boolean identityContains(Collection<Object> addedElements, MutableStateTrackable element) {
        for (Object addedElement : addedElements) {
            if (addedElement == element) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    protected final DirtyAttributeFlusher<X, E, V> getElementOnlyFlusher(UpdateContext context, V current) {
        List<A> actions = new ArrayList<>();
        List<CollectionElementAttributeFlusher<E, V>> elementFlushers = getElementFlushers(context, current, actions);
        // A "null" element flusher list is given when a fetch and compare is more appropriate
        if (elementFlushers == null) {
            if (!actions.isEmpty()) {
                return partialFlusher(true, PluralFlushOperation.COLLECTION_REPLAY_ONLY, actions, Collections.EMPTY_LIST);
            }
            return this;
        }

        if (elementFlushers.isEmpty()) {
            if (!actions.isEmpty()) {
                return partialFlusher(true, PluralFlushOperation.COLLECTION_REPLAY_ONLY, actions, Collections.EMPTY_LIST);
            }
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
        // We don't fetch if the force entity mode is active because that means, an entity is already given
        if (flushStrategy == FlushStrategy.ENTITY && !context.isForceEntity()) {
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
        List<CollectionElementAttributeFlusher<E, V>> elementFlushers = getElementFlushers(context, current, null);
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

    protected abstract List<CollectionElementAttributeFlusher<E, V>> getElementFlushers(UpdateContext context, V current, List<? extends A> actions);

    protected final boolean determineElementFlushers(UpdateContext context, TypeDescriptor typeDescriptor, List<CollectionElementAttributeFlusher<E, V>> elementFlushers, Iterable<?> values, List<? extends A> actions, V current) {
        if (typeDescriptor.shouldFlushMutations()) {
            if (typeDescriptor.isSubview()) {
                final ViewToEntityMapper mapper = typeDescriptor.getViewToEntityMapper();
                if (typeDescriptor.isIdentifiable()) {
                    for (Object o : values) {
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
                    if (typeDescriptor.supportsDirtyCheck() && !typeDescriptor.isIdentifiable() && isIndexed()) {
                        addFlatViewElementFlushActions(context, typeDescriptor, (List<A>) actions, current);
                    } else {
                        for (Object o : values) {
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
                }
            } else if (typeDescriptor.isJpaEntity()) {
                for (Object element : values) {
                    if (typeDescriptor.getBasicUserType().shouldPersist(element) && typeDescriptor.shouldJpaPersist()) {
                        elementFlushers.add(createPersistFlusher(typeDescriptor, element));
                    } else if (element != null && typeDescriptor.shouldJpaMerge()) {
                        elementFlushers.add(createMergeFlusher(typeDescriptor, element));
                    }
                }
            } else if (typeDescriptor.getBasicUserType().supportsDirtyChecking()) {
                for (Object element : values) {
                    String[] dirtyProperties = typeDescriptor.getBasicUserType().getDirtyProperties(element);
                    if (dirtyProperties != null) {
                        // We can't merge basic elements separately so we need to replace the element in the collection
                        // This is signalled by returning null
                        return true;
                    }
                }
            } else if (canFlushSeparateCollectionOperations()) {
                // We can't merge basic elements separately so we need to replace the element in the collection
                // This is signalled by returning null
                return true;
            } else {
                throw new IllegalArgumentException("Element flushers for non-identifiable type not determinable: " + typeDescriptor);
            }
        }

        return false;
    }

    protected abstract boolean canFlushSeparateCollectionOperations();

    protected abstract CollectionElementAttributeFlusher<E, V> createPersistFlusher(TypeDescriptor typeDescriptor, Object element);

    protected abstract CollectionElementAttributeFlusher<E, V> createMergeFlusher(TypeDescriptor typeDescriptor, Object element);

    protected abstract AbstractPluralAttributeFlusher<X, A, R, E, V> partialFlusher(boolean fetch, PluralFlushOperation operation, List<? extends A> collectionActions, List<CollectionElementAttributeFlusher<E, V>> elementFlushers);

    /**
     * @author Christian Beikov
     * @since 1.4.0
     */
    protected static enum EntryState {
        EXISTED {
            @Override
            EntryState onAdd() {
                return EXISTED;
            }

            @Override
            EntryState onRemove() {
                return REMOVED;
            }
        },
        ADDED {
            @Override
            EntryState onAdd() {
                return ADDED;
            }

            @Override
            EntryState onRemove() {
                return EXISTED;
            }
        },
        REMOVED {
            @Override
            EntryState onAdd() {
                return EXISTED;
            }

            @Override
            EntryState onRemove() {
                return REMOVED;
            }
        };

        abstract EntryState onAdd();
        abstract EntryState onRemove();
    }

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
            DirtyAttributeFlusher<?, Object, Object> idFlusher = (DirtyAttributeFlusher<?, Object, Object>) mapper.getIdFlusher();
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
