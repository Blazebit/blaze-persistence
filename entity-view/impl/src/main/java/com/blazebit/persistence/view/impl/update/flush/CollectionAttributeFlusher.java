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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.DeleteCriteriaBuilder;
import com.blazebit.persistence.InsertCriteriaBuilder;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.accessor.InitialValueAttributeAccessor;
import com.blazebit.persistence.view.impl.change.DirtyChecker;
import com.blazebit.persistence.view.impl.collection.CollectionAction;
import com.blazebit.persistence.view.impl.collection.CollectionAddAllAction;
import com.blazebit.persistence.view.impl.collection.CollectionClearAction;
import com.blazebit.persistence.view.impl.collection.CollectionInstantiator;
import com.blazebit.persistence.view.impl.collection.CollectionRemoveAllAction;
import com.blazebit.persistence.view.impl.collection.CollectionRemoveListener;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.proxy.DirtyStateTrackable;
import com.blazebit.persistence.view.impl.proxy.MutableStateTrackable;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CollectionAttributeFlusher<E, V extends Collection<?>> extends AbstractPluralAttributeFlusher<CollectionAttributeFlusher<E, V>, CollectionAction<?>, RecordingCollection<?, ?>, E, V> implements DirtyAttributeFlusher<CollectionAttributeFlusher<E, V>, E, V> {

    private static final Object REMOVED_MARKER = new Object();
    private final CollectionInstantiator collectionInstantiator;
    private final InverseFlusher<E> inverseFlusher;
    private final InverseCollectionElementAttributeFlusher.Strategy inverseRemoveStrategy;

    @SuppressWarnings("unchecked")
    public CollectionAttributeFlusher(String attributeName, String mapping, Class<?> ownerEntityClass, String ownerIdAttributeName, String ownerMapping, DirtyAttributeFlusher<?, ?, ?> ownerIdFlusher, DirtyAttributeFlusher<?, ?, ?> elementFlusher, boolean supportsCollectionDml, FlushStrategy flushStrategy, AttributeAccessor entityAttributeAccessor,
                                      InitialValueAttributeAccessor viewAttributeAccessor, boolean optimisticLockProtected, boolean collectionUpdatable, boolean viewOnlyDeleteCascaded, boolean jpaProviderDeletesCollection, CollectionRemoveListener cascadeDeleteListener, CollectionRemoveListener removeListener, CollectionInstantiator collectionInstantiator,
                                      TypeDescriptor elementDescriptor, InverseFlusher<E> inverseFlusher, InverseRemoveStrategy inverseRemoveStrategy) {
        super(attributeName, mapping, collectionUpdatable || elementDescriptor.shouldFlushMutations(), ownerEntityClass, ownerIdAttributeName, ownerMapping, ownerIdFlusher, elementFlusher, supportsCollectionDml, flushStrategy, entityAttributeAccessor, viewAttributeAccessor, optimisticLockProtected, collectionUpdatable, viewOnlyDeleteCascaded, jpaProviderDeletesCollection,
                cascadeDeleteListener, removeListener, elementDescriptor);
        this.collectionInstantiator = collectionInstantiator;
        this.inverseFlusher = inverseFlusher;
        this.inverseRemoveStrategy = InverseCollectionElementAttributeFlusher.Strategy.of(inverseRemoveStrategy);
    }

    protected CollectionAttributeFlusher(CollectionAttributeFlusher original, boolean fetch) {
        this(original, fetch, null, null, null);
    }

    protected CollectionAttributeFlusher(CollectionAttributeFlusher original, boolean fetch, PluralFlushOperation flushOperation, List<? extends CollectionAction<?>> collectionActions, List<CollectionElementAttributeFlusher<E, V>> elementFlushers) {
        super(original, fetch, flushOperation, collectionActions, elementFlushers);
        this.collectionInstantiator = original.collectionInstantiator;
        this.inverseFlusher = original.inverseFlusher;
        this.inverseRemoveStrategy = original.inverseRemoveStrategy;
    }

    @SuppressWarnings("unchecked")
    protected V createCollection(int size) {
        return (V) collectionInstantiator.createCollection(size);
    }

    @SuppressWarnings("unchecked")
    protected V createJpaCollection(int size) {
        return (V) collectionInstantiator.createJpaCollection(size);
    }

    @Override
    protected V createJpaCollection() {
        return (V) collectionInstantiator.createJpaCollection(0);
    }

    @SuppressWarnings("unchecked")
    protected RecordingCollection<?, ?> createRecordingCollection(int size) {
        return collectionInstantiator.createRecordingCollection(size);
    }

    @Override
    public V cloneDeep(Object view, V oldValue, V newValue) {
        if (newValue == null || newValue.isEmpty()) {
            return newValue;
        }
        if (elementDescriptor.shouldFlushMutations() && !elementDescriptor.isSubview()) {
            BasicUserType<Object> basicUserType = elementDescriptor.getBasicUserType();
            // We only do a collection copy if the element type supports deep cloning
            if (basicUserType != null && !basicUserType.supportsDirtyChecking() && basicUserType.supportsDeepCloning()) {
                V newCollection = createCollection(newValue.size());
                Collection<Object> collection = (Collection<Object>) newCollection;
                for (Object o : newValue) {
                    collection.add(basicUserType.deepClone(o));
                }
                return newCollection;
            }
        }
        return newValue;
    }

    @Override
    public Object getNewInitialValue(UpdateContext context, V clonedValue, V currentValue) {
        BasicUserType<Object> basicUserType = elementDescriptor.getBasicUserType();
        if (elementDescriptor.shouldFlushMutations() && !elementDescriptor.isSubview() && basicUserType != null && basicUserType.supportsDeepCloning() && !basicUserType.supportsDirtyTracking()) {
            return clonedValue;
        } else {
            return currentValue;
        }
    }

    @Override
    protected void invokeCollectionAction(UpdateContext context, Object ownerView, Object view, V targetCollection, Object value, List<? extends CollectionAction<?>> collectionActions) {
        final ViewToEntityMapper viewToEntityMapper = elementDescriptor.getLoadOnlyViewToEntityMapper();
        if (mapping == null) {
            // When the mapping is null this means that there is no collection role in the entity
            // This happens for correlated attributes and we will just provide an empty collection for applying actions
            targetCollection = createCollection(0);
            for (CollectionAction<V> action : (List<CollectionAction<V>>) (List<?>) collectionActions) {
                action.doAction(targetCollection, context, viewToEntityMapper, removeListener);
            }
        } else {
            if (flushStrategy == FlushStrategy.QUERY) {
                flushCollectionOperations(context, ownerView, view, (V) value, null, collectionActions);
            } else {
                // NOTE: We don't care if the actual collection and the initial collection differ
                // If an error is desired, a user should configure optimistic locking
                for (CollectionAction<V> action : (List<CollectionAction<V>>) (List<?>) collectionActions) {
                    action.doAction(targetCollection, context, viewToEntityMapper, removeListener);
                }
            }
        }
    }

    @Override
    protected V replaceWithRecordingCollection(UpdateContext context, Object view, V value, List<? extends CollectionAction<?>> actions) {
        Collection<?> initialState = (Collection<?>) viewAttributeAccessor.getInitialValue(view);
        initialState = initialState != null ? initialState : Collections.emptyList();
        RecordingCollection<Collection<?>, ?> collection;
        if (value instanceof RecordingCollection<?, ?>) {
            collection = (RecordingCollection<Collection<?>, ?>) value;
        } else {
            if (value != null) {
                collection = (RecordingCollection<Collection<?>, ?>) createRecordingCollection(value.size());
                ((Collection<Object>) collection.getDelegate()).addAll(value);
            } else {
                collection = (RecordingCollection<Collection<?>, ?>) createRecordingCollection(0);
            }
            viewAttributeAccessor.setValue(view, collection);
        }
        if (actions != null && !actions.isEmpty() && collection != initialState) {
            collection.initiateActionsAgainstState((List<CollectionAction<Collection<?>>>) actions, initialState);
            collection.resetActions(context);
        }
        V initialValue = cloneDeep(view, null, (V) collection);
        if (initialValue != value) {
            viewAttributeAccessor.setInitialValue(view, initialValue);
        }
        return (V) collection;
    }

    @Override
    public boolean supportsQueryFlush() {
        return inverseFlusher != null && inverseFlusher.supportsQueryFlush() || inverseFlusher == null && super.supportsQueryFlush();
    }

    @Override
    public boolean requiresFlushAfterPersist(V value) {
        if (inverseFlusher != null) {
            return elementFlushers != null || !(value instanceof RecordingCollection<?, ?>) || ((RecordingCollection<Collection<?>, ?>) value).hasActions();
        }

        return false;
    }

    @Override
    public boolean requiresDeferredFlush(V value) {
        return false;
    }

    protected boolean executeActions(UpdateContext context, Collection<Object> jpaCollection, List<CollectionAction<Collection<?>>> actions, ViewToEntityMapper mapper) {
        for (CollectionAction<Collection<?>> action : actions) {
            action.doAction(jpaCollection, context, mapper, removeListener);
        }
        return !actions.isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public FetchGraphNode<?> mergeWith(List<CollectionAttributeFlusher<E, V>> fetchGraphNodes) {
        boolean fetchChanged = false;
        List<FetchGraphNode<?>> nestedFlushers = new ArrayList<>(fetchGraphNodes.size());
        for (int i = 0; i < fetchGraphNodes.size(); i++) {
            CollectionAttributeFlusher<E, V> node = fetchGraphNodes.get(i);
            fetchChanged |= this.fetch != node.fetch;
            FetchGraphNode<?> nestedGraphNode = node.getNestedGraphNode();
            if (nestedGraphNode != null) {
                if (nestedGraphNode instanceof CollectionElementFetchGraphNode) {
                    CollectionElementFetchGraphNode<?, ?> collectionElementFetchGraphNode = (CollectionElementFetchGraphNode<?, ?>) nestedGraphNode;
                    if (collectionElementFetchGraphNode.nestedGraphNode != null) {
                        nestedFlushers.add(collectionElementFetchGraphNode.nestedGraphNode);
                    }
                } else {
                    nestedFlushers.add(nestedGraphNode);
                }
            }
        }

        final boolean newFetch = fetchChanged || this.fetch;

        if (nestedFlushers.isEmpty()) {
            if (fetchChanged && this.fetch != newFetch) {
                return new AttributeFetchGraphNode<>(attributeName, mapping, newFetch, fetchGraphNodes.get(0));
            } else {
                return this;
            }
        }
        FetchGraphNode<?> firstFlusher = nestedFlushers.get(0);
        FetchGraphNode<?> fetchGraphNode = firstFlusher.mergeWith((List) nestedFlushers);

        // All fetch graph nodes have the same structure, so no need for new objects
        if (!fetchChanged && fetchGraphNode == firstFlusher) {
            return this;
        }

        return new AttributeFetchGraphNode<>(attributeName, mapping, newFetch, fetchGraphNode);
    }

    @Override
    public void flushQuery(UpdateContext context, String parameterPrefix, Query query, Object ownerView, Object view, V current, UnmappedOwnerAwareDeleter ownerAwareDeleter) {
        if (!supportsQueryFlush()) {
            throw new UnsupportedOperationException("Query flush not supported for configuration!");
        }

        if (flushOperation != null) {
            if (!(current instanceof RecordingCollection<?, ?>)) {
                List<CollectionAction<Collection<?>>> actions = new ArrayList<>();
                actions.add(new CollectionClearAction());
                if (current != null && !current.isEmpty()) {
                    actions.add(new CollectionAddAllAction(current, collectionInstantiator.allowsDuplicates()));
                }
                current = replaceWithRecordingCollection(context, view, current, actions);
            }
            invokeFlushOperation(context, ownerView, view, null, current);
        } else {
            boolean isRecording = current instanceof RecordingCollection<?, ?>;
            if (isRecording) {
                RecordingCollection<Collection<?>, ?> recordingCollection = (RecordingCollection<Collection<?>, ?>) current;
                if (inverseFlusher != null) {
                    Map<Object, Object> added;
                    Map<Object, Object> removed;
                    if (entityAttributeMapper != null && recordingCollection.hasActions()) {
                        Map<Object, Object>[] addedAndRemoved = getAddedAndRemovedElementsForInverseFlusher(recordingCollection, context);
                        added = addedAndRemoved[0];
                        removed = addedAndRemoved[1];
                    } else {
                        added = removed = Collections.emptyMap();
                    }
                    visitInverseElementFlushersForActions(context, recordingCollection, added, removed, new ElementFlusherQueryExecutor(context, null, ownerView));
                } else {
                    if (entityAttributeMapper == null) {
                        // We have a correlation mapping here
                        recordingCollection.resetActions(context);
                    }

                    List<Object> embeddables = null;
                    if (elementDescriptor.shouldFlushMutations()) {
                        if (elementDescriptor.shouldJpaPersistOrMerge()) {
                            mergeAndRequeue(context, recordingCollection, (Collection<Object>) recordingCollection.getDelegate());
                        } else if (elementDescriptor.isSubview() && (elementDescriptor.isIdentifiable() || isIndexed())) {
                            embeddables = flushCollectionViewElements(context, current);
                        }
                    }

                    if (entityAttributeMapper != null && collectionUpdatable) {
                        V initial = (V) viewAttributeAccessor.getInitialValue(view);
                        if (initial instanceof RecordingCollection<?, ?>) {
                            initial = (V) ((RecordingCollection) initial).getInitialVersion();
                        }
                        recordingCollection.resetActions(context);
                        // If the initial object was null like it happens during full flushing, we can only replace the collection
                        if (initial == null) {
                            replaceCollection(context, ownerView, view, null, current, FlushStrategy.QUERY);
                        } else {
                            flushCollectionOperations(context, ownerView, view, initial, current, embeddables, (FusedCollectionActions) null);
                        }
                    }
                }
            } else {
                EqualityChecker equalityChecker;
                if (elementDescriptor.isSubview()) {
                    equalityChecker = EqualsEqualityChecker.INSTANCE;
                } else {
                    equalityChecker = new IdentityEqualityChecker(elementDescriptor.getBasicUserType());
                }
                V initial = (V) viewAttributeAccessor.getInitialValue(view);
                if (initial instanceof RecordingCollection<?, ?>) {
                    initial = (V) ((RecordingCollection) initial).getInitialVersion();
                }
                List<CollectionAction<Collection<?>>> actions;
                if (initial == null || !elementDescriptor.supportsDeepEqualityCheck() || elementDescriptor.getBasicUserType() != null && !elementDescriptor.getBasicUserType().supportsDeepCloning()) {
                    actions = replaceActions(current);
                } else {
                    actions = determineCollectionActions(context, initial, current, equalityChecker);
                }
                current = replaceWithRecordingCollection(context, view, current, actions);

                List<Object> embeddables = null;
                if (elementDescriptor.shouldFlushMutations()) {
                    if (elementDescriptor.shouldJpaPersistOrMerge()) {
                        mergeAndRequeue(context, null, (Collection<Object>) current);
                    } else if (elementDescriptor.isSubview() && (elementDescriptor.isIdentifiable() || isIndexed())) {
                        embeddables = flushCollectionViewElements(context, current);
                    }
                }

                if (entityAttributeMapper != null && collectionUpdatable) {
                    // If the initial object was null like it happens during full flushing, we can only replace the collection
                    if (initial == null) {
                        replaceCollection(context, ownerView, view, null, current, FlushStrategy.QUERY);
                    } else {
                        flushCollectionOperations(context, ownerView, view, initial, current, embeddables, (FusedCollectionActions) null);
                    }
                }
            }
        }
    }

    protected final DeleteCriteriaBuilder<?> createCollectionDeleter(UpdateContext context) {
        DeleteCriteriaBuilder<?> deleteCb = context.getEntityViewManager().getCriteriaBuilderFactory().deleteCollection(context.getEntityManager(), ownerEntityClass, "e", getMapping());
        deleteCb.setWhereExpression(ownerIdWhereFragment);
        return deleteCb;
    }

    protected Collection<Object> appendRemoveSpecific(UpdateContext context, DeleteCriteriaBuilder<?> deleteCb, FusedCollectionActions fusedCollectionActions) {
        deleteCb.where("e." + getMapping()).in(fusedCollectionActions.getRemoved(context));
        return new HashSet<>(fusedCollectionActions.getRemoved());
    }

    protected List<Object> getEntityReferencesForCollectionOperation(UpdateContext context, Collection<Object> objects) {
        List<Object> entityReferences = new ArrayList<>(objects.size());
        ViewToEntityMapper loadOnlyViewToEntityMapper = elementDescriptor.getLoadOnlyViewToEntityMapper();
        for (Object o : objects) {
            if (o != null) {
                entityReferences.add(loadOnlyViewToEntityMapper.applyToEntity(context, null, o));
            }
        }
        return entityReferences;
    }

    protected boolean deleteElements(UpdateContext context, Object ownerView, Object view, V value, boolean removeSpecific, FusedCollectionActions fusedCollectionActions) {
        DeleteCriteriaBuilder<?> deleteCb = null;
        boolean removedAll = true;
        Collection<Object> removedObjects = Collections.emptyList();
        if (fusedCollectionActions != null) {
            if (fusedCollectionActions.getRemoveCount() > 0) {
                deleteCb = createCollectionDeleter(context);
                if (removeSpecific) {
                    String entityIdAttributeName = elementDescriptor.getEntityIdAttributeName();
                    if (entityIdAttributeName != null) {
                        removedObjects = appendRemoveSpecific(context, deleteCb, fusedCollectionActions);
                        removedAll = false;
                        if (removedObjects.isEmpty()) {
                            deleteCb = null;
                        }
                    }
                }
            } else {
                removedAll = false;
            }
        } else {
            deleteCb = createCollectionDeleter(context);
        }

        if (deleteCb != null) {
            Query deleteQuery = deleteCb.getQuery();
            ownerIdFlusher.flushQuery(context, null, deleteQuery, ownerView, view, ownerIdFlusher.getViewAttributeAccessor().getValue(ownerView), null);
            deleteQuery.executeUpdate();
            if (removedAll) {
                return true;
            }
            if (removeListener != null) {
                for (Object removedObject : removedObjects) {
                    removeListener.onCollectionRemove(context, removedObject);
                }
            }
        }

        // TODO: Think about allowing to use batching when we implement #657

        return false;
    }

    protected void addElements(UpdateContext context, Object ownerView, Object view, Collection<Object> removedAllObjects, boolean flushAtOnce, V value, List<Object> embeddablesToUpdate, FusedCollectionActions fusedCollectionActions) {
        Collection<Object> elementsToAdd;
        if (fusedCollectionActions == null || !removedAllObjects.isEmpty()) {
            if (elementDescriptor.getViewToEntityMapper() == null) {
                elementsToAdd = (Collection<Object>) value;
            } else {
                elementsToAdd = getEntityReferencesForCollectionOperation(context, (Collection<Object>) value);
            }
            removedAllObjects.removeAll(value);
        } else {
            removedAllObjects.removeAll(fusedCollectionActions.getAdded());
            elementsToAdd = fusedCollectionActions.getAdded(context);
        }
        if (elementsToAdd == null || elementsToAdd.isEmpty() || elementsToAdd.size() == 1 && elementsToAdd.iterator().next() == null) {
            return;
        }

        String mapping = getMapping();
        InsertCriteriaBuilder<?> insertCb = context.getEntityViewManager().getCriteriaBuilderFactory().insertCollection(context.getEntityManager(), ownerEntityClass, mapping);

        String entityIdAttributeName = elementDescriptor.getEntityIdAttributeName();
        if (flushAtOnce) {
            if (entityIdAttributeName == null) {
                insertCb.fromValues(ownerEntityClass, mapping, "val", elementsToAdd);
            } else {
                insertCb.fromIdentifiableValues((Class<Object>) elementDescriptor.getJpaType(), "val", elementsToAdd);
            }
        } else {
            if (entityIdAttributeName == null) {
                insertCb.fromValues(ownerEntityClass, mapping, "val", 1);
            } else {
                insertCb.fromIdentifiableValues((Class<Object>) elementDescriptor.getJpaType(), "val", 1);
            }
        }
        for (int i = 0; i < ownerIdBindFragments.length; i += 2) {
            insertCb.bind(ownerIdBindFragments[i]).select(ownerIdBindFragments[i + 1]);
        }
        insertCb.bind(mapping).select("val");
        Query insertQuery = insertCb.getQuery();
        ownerIdFlusher.flushQuery(context, null, insertQuery, ownerView, view, ownerIdFlusher.getViewAttributeAccessor().getValue(ownerView), null);

        boolean checkTransient = elementDescriptor.isJpaEntity() && !elementDescriptor.shouldJpaPersist();
        if (flushAtOnce) {
            if (checkTransient) {
                for (Object o : elementsToAdd) {
                    if (elementDescriptor.getBasicUserType().shouldPersist(o)) {
                        throw new IllegalStateException("Collection " + attributeName + " references an unsaved transient instance - save the transient instance before flushing: " + o);
                    }
                }
            }
            insertQuery.executeUpdate();
        } else {
            // TODO: Use batching when we implement #657
            Object[] singletonArray = new Object[1];
            List<Object> singletonList = Arrays.asList(singletonArray);
            for (Object o : elementsToAdd) {
                if (o != null) {
                    if (checkTransient && elementDescriptor.getBasicUserType().shouldPersist(o)) {
                        throw new IllegalStateException("Collection " + attributeName + " references an unsaved transient instance - save the transient instance before flushing: " + o);
                    }
                    singletonArray[0] = o;
                    insertQuery.setParameter("val", singletonList);
                    insertQuery.executeUpdate();
                }
            }
        }
    }

    @Override
    protected boolean canFlushSeparateCollectionOperations() {
        return !collectionInstantiator.allowsDuplicates();
    }

    @Override
    protected boolean isIndexed() {
        return false;
    }

    @Override
    protected void addFlatViewElementFlushActions(UpdateContext context, TypeDescriptor elementDescriptor, List<CollectionAction<?>> actions, V current) {
        throw new UnsupportedOperationException("Not indexed!");
    }

    protected void flushCollectionOperations(UpdateContext context, Object ownerView, Object view, V value, List<Object> embeddablesToUpdate, List<? extends CollectionAction<?>> collectionActions) {
        FusedCollectionActions fusedCollectionActions = null;
        // We can't selectively delete/add if duplicates are allowed. Bags always need to be recreated
        if (canFlushSeparateCollectionOperations()) {
            if (collectionActions.isEmpty()) {
                if (embeddablesToUpdate != null) {
                    addElements(context, ownerView, view, Collections.emptyList(), true, value, embeddablesToUpdate, null);
                }
                return;
            } else if (!(collectionActions.get(0) instanceof CollectionClearAction<?, ?>)) {
                // The replace action is handled specially
                fusedCollectionActions = getFusedOperations(collectionActions);
            }
        }
        flushCollectionOperations(context, ownerView, view, null, value, embeddablesToUpdate, fusedCollectionActions);
    }

    protected void flushCollectionOperations(UpdateContext context, Object ownerView, Object view, V initial, V value, List<Object> embeddablesToUpdate, FusedCollectionActions fusedCollectionActions) {
        boolean removeSpecific = fusedCollectionActions != null && fusedCollectionActions.operationCount() < value.size() + 1;
        Collection<Object> removedAllObjects;
        if (deleteElements(context, ownerView, view, value, removeSpecific, fusedCollectionActions)) {
            if (fusedCollectionActions == null) {
                if (initial == null) {
                    removedAllObjects = Collections.emptyList();
                } else {
                    removedAllObjects = new ArrayList<>(initial);
                }
            } else {
                removedAllObjects = new ArrayList<>(fusedCollectionActions.getRemoved());
            }
        } else {
            removedAllObjects = Collections.emptyList();
        }
        addElements(context, ownerView, view, removedAllObjects, true, value, embeddablesToUpdate, fusedCollectionActions);
        if (removeListener != null) {
            for (Object removedObject : removedAllObjects) {
                removeListener.onCollectionRemove(context, removedObject);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean flushEntity(UpdateContext context, E entity, Object ownerView, Object view, V value, Runnable postReplaceListener) {
        if (flushOperation != null) {
            if (!(value instanceof RecordingCollection<?, ?>)) {
                value = replaceWithRecordingCollection(context, view, value, collectionActions);
            }
            invokeFlushOperation(context, ownerView, view, entity, value);
            return true;
        }
        if (collectionUpdatable) {
            boolean replace = false;
            boolean wasDirty = false;
            boolean isRecording = value instanceof RecordingCollection<?, ?>;
            List<CollectionAction<Collection<?>>> actions = null;
            if (isRecording) {
                RecordingCollection<Collection<?>, ?> recordingCollection = (RecordingCollection<Collection<?>, ?>) value;

                if (inverseFlusher != null) {
                    Map<Object, Object> added;
                    Map<Object, Object> removed;
                    if (recordingCollection.hasActions()) {
                        Map<Object, Object>[] addedAndRemoved = getAddedAndRemovedElementsForInverseFlusher(recordingCollection, context);
                        added = addedAndRemoved[0];
                        removed = addedAndRemoved[1];
                    } else {
                        added = removed = Collections.emptyMap();
                    }
                    // It could be the case that entity flushing is triggered by a different dirty collection,
                    // yet we still want elements of this collection to flush with query flushing if configured
                    if (flushStrategy == FlushStrategy.ENTITY || !inverseFlusher.supportsQueryFlush()) {
                        visitInverseElementFlushersForActions(context, recordingCollection, added, removed, new ElementFlusherEntityExecutor(context, entity));
                    } else {
                        visitInverseElementFlushersForActions(context, recordingCollection, added, removed, new ElementFlusherQueryExecutor(context, entity, null));
                    }
                    return true;
                }

                if (elementDescriptor.shouldFlushMutations()) {
                    if (elementDescriptor.shouldJpaPersistOrMerge()) {
                        wasDirty |= mergeAndRequeue(context, recordingCollection, (Collection<Object>) recordingCollection.getDelegate());
                    } else if (elementDescriptor.isSubview() && elementDescriptor.isIdentifiable()) {
                        flushCollectionViewElements(context, value);
                        wasDirty = true;
                    } else {
                        if (fetch && elementDescriptor.supportsDeepEqualityCheck() && entityAttributeMapper != null) {
                            Collection<Object> jpaCollection = (Collection<Object>) entityAttributeMapper.getValue(entity);

                            if (jpaCollection == null || jpaCollection.isEmpty()) {
                                replace = true;
                            } else {
                                actions = determineJpaCollectionActions(context, (V) jpaCollection, value, elementEqualityChecker);

                                if (actions.size() > value.size()) {
                                    // More collection actions means more statements are issued
                                    // We'd rather replace in such a case
                                    replace = true;
                                } else {
                                    return executeActions(context, jpaCollection, actions, elementDescriptor.getLoadOnlyViewToEntityMapper());
                                }
                            }
                        } else {
                            // Non-identifiable mutable elements can't be updated, but have to be replaced
                            replace = true;
                        }
                    }
                }

                if (!replace) {
                    if (entityAttributeMapper == null) {
                        // When having a correlated attribute, we consider is being dirty when it changed
                        wasDirty |= !recordingCollection.resetActions(context).isEmpty();
                    } else {
                        Collection<?> collection = (Collection<?>) entityAttributeMapper.getValue(entity);
                        if (collection == null) {
                            replace = true;
                        } else {
                            wasDirty |= recordingCollection.hasActions();
                            recordingCollection.replay(collection, context, elementDescriptor.getLoadOnlyViewToEntityMapper(), removeListener);
                        }
                    }
                }
            } else {
                actions = replaceActions(value);
                value = replaceWithRecordingCollection(context, view, value, actions);

                if (fetch) {
                    if (inverseFlusher != null) {
                        // When we know the collection was fetched, we can try to "merge" the changes into the JPA collection
                        // If either of the collections is empty, we simply do the replace logic
                        Collection<Object> jpaCollection = (Collection<Object>) entityAttributeMapper.getValue(entity);
                        actions = determineJpaCollectionActions(context, (V) jpaCollection, value, elementEqualityChecker);
                        Map<Object, Object> added;
                        Map<Object, Object> removed;
                        if (!actions.isEmpty()) {
                            Map<Object, Object>[] addedAndRemoved = getAddedAndRemovedElementsForInverseFlusher(actions);
                            added = addedAndRemoved[0];
                            removed = addedAndRemoved[1];
                        } else {
                            added = removed = Collections.emptyMap();
                        }

                        // It could be the case that entity flushing is triggered by a different dirty collection,
                        // yet we still want elements of this collection to flush with query flushing if configured
                        if (flushStrategy == FlushStrategy.ENTITY || !inverseFlusher.supportsQueryFlush()) {
                            visitInverseElementFlushersForActions(context, value, added, removed, new ElementFlusherEntityExecutor(context, entity));
                        } else {
                            visitInverseElementFlushersForActions(context, value, added, removed, new ElementFlusherQueryExecutor(context, entity, null));
                        }
                        return true;
                    }

                    if (value == null || value.isEmpty()) {
                        replace = true;
                    } else if (elementDescriptor.shouldFlushMutations()) {
                        if (elementDescriptor.shouldJpaPersistOrMerge()) {
                            wasDirty |= mergeAndRequeue(context, null, (Collection<Object>) value);
                        } else if (elementDescriptor.isSubview()) {
                            // Apply cascading updates to identifiable subviews
                            if (elementDescriptor.isIdentifiable()) {
                                flushCollectionViewElements(context, value);
                                wasDirty = true;
                            }
                        } else if (!elementDescriptor.supportsDeepEqualityCheck()) {
                            replace = true;
                        }
                    }

                    if (!replace) {
                        if (entityAttributeMapper == null) {
                            // When having a correlated attribute, we consider is being dirty when it changed
                            wasDirty = true;
                        } else {
                            // When we know the collection was fetched, we can try to "merge" the changes into the JPA collection
                            // If either of the collections is empty, we simply do the replace logic
                            Collection<Object> jpaCollection = (Collection<Object>) entityAttributeMapper.getValue(entity);
                            if (jpaCollection == null || jpaCollection.isEmpty()) {
                                replace = true;
                            } else {
                                actions = determineJpaCollectionActions(context, (V) jpaCollection, value, elementEqualityChecker);

                                if (actions.size() > value.size()) {
                                    // More collection actions means more statements are issued
                                    // We'd rather replace in such a case
                                    replace = true;
                                } else {
                                    wasDirty |= executeActions(context, jpaCollection, actions, elementDescriptor.getLoadOnlyViewToEntityMapper());
                                }
                            }
                        }
                    }
                } else {
                    // Always replace the contents of the collection if we don't have a recording collection available
                    replace = true;
                }
            }

            if (replace) {
                if (isRecording) {
                    ((RecordingCollection<Collection<?>, ?>) value).resetActions(context);
                }
                replaceCollection(context, ownerView, view, entity, value, FlushStrategy.ENTITY);
                return true;
            }
            return wasDirty;
        } else if (elementDescriptor.shouldFlushMutations()) {
            if (value != null && !value.isEmpty()) {
                return mergeCollectionElements(context, ownerView, view, entity, value);
            }
            return false;
        } else {
            // Only pass through is possible here
            replaceCollection(context, ownerView, view, entity, value, FlushStrategy.ENTITY);
            return true;
        }
    }

    protected List<CollectionAction<Collection<?>>> replaceActions(V value) {
        List<CollectionAction<Collection<?>>> actions = new ArrayList<>();
        actions.add(new CollectionClearAction());
        if (value != null && !value.isEmpty()) {
            actions.add(new CollectionAddAllAction(value, collectionInstantiator.allowsDuplicates()));
        }
        return actions;
    }

    @Override
    public List<PostFlushDeleter> remove(UpdateContext context, E entity, Object view, V value) {
        V collection;
        if (view instanceof DirtyStateTrackable) {
            collection = (V) viewAttributeAccessor.getInitialValue(view);
        } else {
            collection = value;
        }

        if (collection != null && !collection.isEmpty()) {
            // Entity flushing will do the delete anyway, so we can skip this
            if (flushStrategy == FlushStrategy.QUERY && !jpaProviderDeletesCollection) {
                removeByOwnerId(context, ((EntityViewProxy) view).$$_getId(), false);
            }
            if (cascadeDeleteListener != null) {
                List<Object> elements;
                if (collection instanceof RecordingCollection<?, ?>) {
                    RecordingCollection<?, ?> recordingCollection = (RecordingCollection<?, ?>) collection;
                    Set<?> removedElements = recordingCollection.getRemovedElements();
                    Set<?> addedElements = recordingCollection.getAddedElements();
                    elements = new ArrayList<>(collection.size() + removedElements.size());

                    for (Object element : collection) {
                        // Only report removes for objects that previously existed
                        if (!addedElements.contains(element)) {
                            elements.add(element);
                        }
                    }

                    // Report removed object that would have previously existed as removed
                    elements.addAll(removedElements);
                } else {
                    elements = new ArrayList<>(collection);
                }
                if (elements.size() > 0) {
                    if (inverseFlusher == null) {
                        return Collections.<PostFlushDeleter>singletonList(new PostFlushCollectionElementDeleter(cascadeDeleteListener, elements));
                    } else {
                        // Invoke deletes immediately for inverse relations
                        for (Object element : elements) {
                            cascadeDeleteListener.onCollectionRemove(context, element);
                        }
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    @Override
    public List<PostFlushDeleter> removeByOwnerId(UpdateContext context, Object id) {
        return removeByOwnerId(context, id, true);
    }

    private List<PostFlushDeleter> removeByOwnerId(UpdateContext context, Object ownerId, boolean cascade) {
        EntityViewManagerImpl evm = context.getEntityViewManager();
        String mapping = getMapping();
        if (cascade) {
            List<Object> elementIds;
            if (inverseFlusher == null) {
                // If there is no inverseFlusher/mapped by attribute, the collection has a join table
                if (evm.getDbmsDialect().supportsReturningColumns()) {
                    List<Tuple> tuples = evm.getCriteriaBuilderFactory().deleteCollection(context.getEntityManager(), ownerEntityClass, "e", mapping)
                            .where(ownerIdAttributeName).eq(ownerId)
                            .executeWithReturning(mapping + "." + elementDescriptor.getEntityIdAttributeName())
                            .getResultList();

                    elementIds = new ArrayList<>(tuples.size());
                    for (Tuple tuple : tuples) {
                        elementIds.add(tuple.get(0));
                    }
                } else {
                    elementIds = (List<Object>) evm.getCriteriaBuilderFactory().create(context.getEntityManager(), ownerEntityClass, "e")
                            .where(ownerIdAttributeName).eq(ownerId)
                            .select("e." + mapping + "." + elementDescriptor.getEntityIdAttributeName())
                            .getResultList();
                    if (!elementIds.isEmpty() && !jpaProviderDeletesCollection) {
                        // We must always delete this, otherwise we might get a constraint violation because of the cascading delete
                        DeleteCriteriaBuilder<?> cb = evm.getCriteriaBuilderFactory().deleteCollection(context.getEntityManager(), ownerEntityClass, "e", mapping);
                        cb.where(ownerIdAttributeName).eq(ownerId);
                        cb.executeUpdate();
                    }
                }

                return Collections.<PostFlushDeleter>singletonList(new PostFlushCollectionElementByIdDeleter(elementDescriptor.getElementToEntityMapper(), elementIds));
            } else {
                return inverseFlusher.removeByOwnerId(context, ownerId);
            }
        } else if (!jpaProviderDeletesCollection) {
            // delete from Entity(collectionRole) e where e.id = :id
            DeleteCriteriaBuilder<?> cb = evm.getCriteriaBuilderFactory().deleteCollection(context.getEntityManager(), ownerEntityClass, "e", mapping);
            cb.where(ownerIdAttributeName).eq(ownerId);
            cb.executeUpdate();
        }

        return Collections.emptyList();
    }

    @Override
    public void remove(UpdateContext context, Object id) {
        throw new UnsupportedOperationException("Unsupported!");
    }

    @Override
    public void removeFromEntity(UpdateContext context, E entity) {
        V value = (V) entityAttributeMapper.getValue(entity);

        if (value != null) {
            // In any case we clear the collection
            if (cascadeDeleteListener != null) {
                if (!value.isEmpty()) {
                    for (Object element : value) {
                        cascadeDeleteListener.onEntityCollectionRemove(context, element);
                    }
                    entityAttributeMapper.setValue(entity, null);
                }
            } else {
                value.clear();
            }
        }
    }

    @Override
    public boolean requiresDeleteCascadeAfterRemove() {
        return false;
    }

    @Override
    public boolean isViewOnlyDeleteCascaded() {
        return viewOnlyDeleteCascaded;
    }

    @Override
    protected boolean mergeCollectionElements(UpdateContext context, Object ownerView, Object view, E entity, V value) {
        if (elementFlushers != null) {
            if (flushStrategy == FlushStrategy.ENTITY || !supportsQueryFlush()) {
                for (CollectionElementAttributeFlusher<E, V> elementFlusher : elementFlushers) {
                    elementFlusher.flushEntity(context, entity, ownerView, view, value, null);
                }
            } else {
                for (CollectionElementAttributeFlusher<E, V> elementFlusher : elementFlushers) {
                    elementFlusher.flushQuery(context, null, null, ownerView, view, value, null);
                }
            }
            return !elementFlushers.isEmpty();
        } else {
            // Invocations of JPA merge can change the identity that leads to requeuing into the collection being required
            final boolean needsRequeuing = elementDescriptor.shouldJpaMerge();

            if (needsRequeuing) {
                if (value instanceof RecordingCollection<?, ?>) {
                    return mergeAndRequeue(context, (RecordingCollection<?, ?>) value, ((RecordingCollection) value).getDelegate());
                } else {
                    return mergeAndRequeue(context, null, (Collection<Object>) value);
                }
            } else if (elementDescriptor.isSubview()) {
                flushCollectionViewElements(context, value);
                return true;
            } else if (elementDescriptor.shouldJpaPersist()) {
                EntityManager em = context.getEntityManager();
                BasicUserType<Object> basicUserType = elementDescriptor.getBasicUserType();
                for (Object o : value) {
                    persistIfNeeded(em, o, basicUserType);
                }
                return true;
            }

            return false;
        }
    }

    protected boolean mergeAndRequeue(UpdateContext context, RecordingCollection recordingCollection, Collection<Object> newCollection) {
        EntityManager em = context.getEntityManager();
        Collection<Object> queuedElements = null;
        Iterator<?> iter = newCollection.iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            Object merged = persistOrMerge(em, o);

            if (o != merged) {
                if (queuedElements == null) {
                    queuedElements = (Collection<Object>) createCollection(newCollection.size());
                }
                iter.remove();
                queuedElements.add(merged);
                if (recordingCollection != null) {
                    recordingCollection.replaceActionElement(o, merged);
                }
            }
        }

        if (queuedElements != null) {
            newCollection.addAll(queuedElements);
        }
        return true;
    }

    private List<Object> flushCollectionViewElements(UpdateContext context, V value) {
        final ViewToEntityMapper viewToEntityMapper = elementDescriptor.getViewToEntityMapper();
        final Iterator<Object> iter = getRecordingIterator(value);
        List<Object> embeddables = new ArrayList<>();
        try {
            while (iter.hasNext()) {
                Object elem = iter.next();
                Object embeddable = viewToEntityMapper.applyToEntity(context, null, elem);
                if (!elementDescriptor.isIdentifiable() && mapping != null) {
                    // Only query flushing of an element collection can bring us here
                    embeddables.add(embeddable);
                }
            }
        } finally {
            resetRecordingIterator(value);
        }
        return embeddables;
    }

    @Override
    protected void replaceCollection(UpdateContext context, Object ownerView, Object view, E entity, V value, FlushStrategy flushStrategy) {
        if (flushStrategy == FlushStrategy.QUERY) {
            Collection<Object> removedAllObjects;
            if (deleteElements(context, ownerView, view, value, false, null)) {
                // TODO: We should load the initial value
                removedAllObjects = Collections.emptyList();
            } else {
                removedAllObjects = Collections.emptyList();
            }
            addElements(context, ownerView, view, removedAllObjects, true, value, null, null);
            if (removeListener != null) {
                for (Object removedObject : removedAllObjects) {
                    removeListener.onCollectionRemove(context, removedObject);
                }
            }
        } else {
            if (entityAttributeMapper != null) {
                if (elementDescriptor.isSubview()) {
                    Collection<Object> newCollection;
                    if (value == null) {
                        newCollection = (Collection<Object>) createJpaCollection(0);
                    } else {
                        newCollection = (Collection<Object>) createJpaCollection(value.size());
                        final ViewToEntityMapper viewToEntityMapper = elementDescriptor.getViewToEntityMapper();
                        final Iterator<Object> iter = getRecordingIterator(value);
                        try {
                            while (iter.hasNext()) {
                                Object elem = iter.next();
                                newCollection.add(viewToEntityMapper.applyToEntity(context, null, elem));
                            }
                        } finally {
                            resetRecordingIterator(value);
                        }
                    }
                    entityAttributeMapper.setValue(entity, newCollection);
                } else {
                    entityAttributeMapper.setValue(entity, value);
                }
                // TODO: collectionRemoveListener?
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Iterator<Object> getRecordingIterator(V value) {
        // TODO: only create a recording iterator when the mapper can have creatable types
        if (value instanceof RecordingCollection<?, ?> && elementDescriptor.getViewToEntityMapper() != null) {
            return (Iterator<Object>) ((RecordingCollection<?, ?>) value).recordingIterator();
        }

        return (Iterator<Object>) value.iterator();
    }

    @SuppressWarnings("unchecked")
    private void resetRecordingIterator(V value) {
        if (value instanceof RecordingCollection<?, ?>) {
            ((RecordingCollection<?, ?>) value).resetRecordingIterator();
        }
    }

    @Override
    public <X> DirtyChecker<X>[] getNestedCheckers(V current) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DirtyChecker<E> getElementDirtyChecker(E element) {
        if (!elementDescriptor.shouldFlushMutations()) {
            return null;
        }
        if (elementDescriptor.isSubview()) {
            return (DirtyChecker<E>) elementDescriptor.getViewToEntityMapper().getUpdater(element).getDirtyChecker();
        } else if (elementDescriptor.isJpaEntity()) {
            return (DirtyChecker<E>) elementDescriptor.getEntityToEntityMapper().getDirtyChecker();
        } else {
            return (DirtyChecker<E>) elementDirtyChecker;
        }
    }

    @Override
    public DirtyKind getDirtyKind(V initial, V current) {
        if (current == null) {
            if (initial == null) {
                return DirtyKind.NONE;
            }
            return DirtyKind.UPDATED;
        }
        if (initial == null) {
            return DirtyKind.UPDATED;
        }

        if (initial == current) {
            if (current instanceof RecordingCollection<?, ?>) {
                RecordingCollection<?, ?> recordingCollection = (RecordingCollection<?, ?>) current;
                if (recordingCollection.hasActions()) {
                    return DirtyKind.MUTATED;
                }

                if (elementDescriptor.shouldFlushMutations()) {
                    if (elementDescriptor.supportsDirtyCheck()) {
                        if (elementDescriptor.isSubview()) {
                            if (!recordingCollection.$$_isDirty()) {
                                return DirtyKind.NONE;
                            }

                            ViewToEntityMapper mapper = elementDescriptor.getViewToEntityMapper();
                            for (Object o : recordingCollection) {
                                if (o instanceof DirtyStateTrackable) {
                                    DirtyStateTrackable element = (DirtyStateTrackable) o;
                                    if (mapper.getUpdater(o).getDirtyChecker().getDirtyKind(element, element) != DirtyKind.NONE) {
                                        return DirtyKind.MUTATED;
                                    }
                                }
                            }
                        } else {
                            BasicUserType<Object> userType = elementDescriptor.getBasicUserType();
                            for (Object o : recordingCollection) {
                                String[] dirtyProperties = userType.getDirtyProperties(o);
                                if (dirtyProperties != null) {
                                    return DirtyKind.MUTATED;
                                }
                            }
                        }
                    } else {
                        // If we don't support dirty checking we always have to assume dirtyness
                        return DirtyKind.MUTATED;
                    }
                } else {
                    // Since initial == current, nothing changed
                    return DirtyKind.NONE;
                }
            }
        } else {
            if (initial.size() != current.size()) {
                return DirtyKind.MUTATED;
            }
            if (elementDescriptor.shouldFlushMutations()) {
                if (elementDescriptor.supportsDirtyCheck()) {
                    if (elementDescriptor.isSubview()) {
                        ViewToEntityMapper mapper = elementDescriptor.getViewToEntityMapper();
                        for (Object o : current) {
                            if (!initial.contains(o)) {
                                return DirtyKind.MUTATED;
                            }
                            if (o instanceof DirtyStateTrackable) {
                                DirtyStateTrackable element = (DirtyStateTrackable) o;
                                if (mapper.getUpdater(o).getDirtyChecker().getDirtyKind(element, element) != DirtyKind.NONE) {
                                    return DirtyKind.MUTATED;
                                }
                            }
                        }
                    } else {
                        BasicUserType<Object> userType = elementDescriptor.getBasicUserType();
                        for (Object o : current) {
                            if (!initial.contains(o)) {
                                return DirtyKind.MUTATED;
                            }
                            String[] dirtyProperties = userType.getDirtyProperties(o);
                            if (dirtyProperties != null) {
                                return DirtyKind.MUTATED;
                            }
                        }
                    }
                } else {
                    if (elementDescriptor.getBasicUserType().supportsDeepCloning()) {
                        return collectionEquals(initial, current) ? DirtyKind.NONE : DirtyKind.MUTATED;
                    } else {
                        // If we don't support dirty checking we always have to assume dirtyness
                        return DirtyKind.MUTATED;
                    }
                }
            } else {
                return collectionEquals(initial, current) ? DirtyKind.NONE : DirtyKind.MUTATED;
            }
        }

        return DirtyKind.NONE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public DirtyAttributeFlusher<CollectionAttributeFlusher<E, V>, E, V> getDirtyFlusher(UpdateContext context, Object view, Object initial, Object current) {
        if (collectionUpdatable) {
            if (initial != current) {
                // If the new collection is empty, we don't need to load the old one
                if (current == null || ((Collection<?>) current).isEmpty()) {
                    if (initial == null || ((Collection<?>) initial).isEmpty()) {
                        return null;
                    }
                    if (inverseFlusher != null) {
                        // TODO: should "replace" mean that the initial values we know right now are "removed" or the actual current values?
                        Map<Object, Object> added = Collections.emptyMap();
                        Map<Object, Object> removed = new IdentityHashMap<>();
                        if (initial != null) {
                            for (Object o : (Collection<?>) initial) {
                                removed.put(o, (Object) o);
                            }
                        }

                        List<CollectionElementAttributeFlusher<E, V>> elementFlushers = getInverseElementFlushersForActions(context, (Collection<?>) current, added, removed);
                        return partialFlusher(false, PluralFlushOperation.COLLECTION_REPLACE_AND_ELEMENT, Collections.EMPTY_LIST, elementFlushers);
                    }
                    return partialFlusher(false, PluralFlushOperation.COLLECTION_REPLACE_ONLY, Collections.EMPTY_LIST, Collections.<CollectionElementAttributeFlusher<E, V>>emptyList());
                }
                // If the initial collection is empty, we also don't need to load the old one
                if (initial == null || ((Collection<?>) initial).isEmpty()) {
                    // Always reset the actions as that indicates changes
                    if (current instanceof RecordingCollection<?, ?>) {
                        ((RecordingCollection<?, ?>) current).resetActions(context);
                    }
                    if (inverseFlusher != null) {
                        // TODO: Should "replace" mean that we also remove values that were added in the meantime?
                        Map<Object, Object> added = new IdentityHashMap<>();
                        Map<Object, Object> removed = Collections.emptyMap();
                        for (Object o : (Collection<?>) current) {
                            added.put(o, (Object) o);
                        }

                        List<CollectionElementAttributeFlusher<E, V>> elementFlushers = getInverseElementFlushersForActions(context, (Collection<?>) current, added, removed);
                        return partialFlusher(false, PluralFlushOperation.COLLECTION_REPLACE_AND_ELEMENT, Collections.EMPTY_LIST, elementFlushers);
                    }
                    return partialFlusher(false, PluralFlushOperation.COLLECTION_REPLACE_ONLY, Collections.EMPTY_LIST, Collections.<CollectionElementAttributeFlusher<E, V>>emptyList());
                }
                if (initial instanceof RecordingCollection<?, ?>) {
                    initial = ((RecordingCollection<?, ?>) initial).getInitialVersion();
                }

                // If the elements are mutable, replacing the collection and merging elements might lead to N+1 queries
                // Since collections rarely change drastically, loading the old collection it is probably a good way to avoid many queries
                if (elementDescriptor.shouldFlushMutations()) {
                    if (elementDescriptor.supportsDirtyCheck()) {
                        // Check elements for dirtyness
                        return determineDirtyFlusherForNewCollection(context, (V) initial, (V) current);
                    } else if (elementDescriptor.supportsDeepEqualityCheck()) {
                        if (canFlushSeparateCollectionOperations() && elementDescriptor.getBasicUserType() != null && elementDescriptor.getBasicUserType().supportsDeepCloning()) {
                            // If we can determine equality, we fetch and merge or replace the elements
                            EqualityChecker equalityChecker;
                            if (elementDescriptor.isSubview()) {
                                equalityChecker = EqualsEqualityChecker.INSTANCE;
                            } else {
                                equalityChecker = new IdentityEqualityChecker(elementDescriptor.getBasicUserType());
                            }
                            List<CollectionAction<Collection<?>>> actions;
                            if (initial == null) {
                                actions = replaceActions((V) current);
                            } else {
                                actions = determineCollectionActions(context, (V) initial, (V) current, equalityChecker);
                            }
                            if (actions.isEmpty()) {
                                return null;
                            }
                            return partialFlusher(true, PluralFlushOperation.COLLECTION_REPLAY_ONLY, actions, Collections.<CollectionElementAttributeFlusher<E, V>>emptyList());
                        } else {
                            return this;
                        }
                    } else if (elementDescriptor.isJpaEntity()) {
                        // When we have a JPA entity, we fetch everything
                        EqualityChecker equalityChecker;
                        if (elementDescriptor.isSubview()) {
                            equalityChecker = EqualsEqualityChecker.INSTANCE;
                        } else {
                            equalityChecker = new IdentityEqualityChecker(elementDescriptor.getBasicUserType());
                        }
                        List<CollectionAction<Collection<?>>> actions;
                        if (initial == null) {
                            actions = replaceActions((V) current);
                        } else {
                            actions = determineCollectionActions(context, (V) initial, (V) current, equalityChecker);
                        }
                        return partialFlusher(true, PluralFlushOperation.COLLECTION_REPLAY_AND_ELEMENT, actions, getElementFlushers(context, (V) current, actions));
                    } else {
                        // Always reset the actions as that indicates changes
                        if (current instanceof RecordingCollection<?, ?>) {
                            ((RecordingCollection<?, ?>) current).resetActions(context);
                        }
                        // Other types are mutable basic types that aren't known to us like e.g. java.util.Date would be if we hadn't registered it
                        return partialFlusher(false, PluralFlushOperation.COLLECTION_REPLACE_ONLY, Collections.EMPTY_LIST, Collections.<CollectionElementAttributeFlusher<E, V>>emptyList());
                    }
                } else {
                    return determineDirtyFlusherForNewCollection(context, (V) initial, (V) current);
                }
            } else {
                // If the initial and current reference are null or empty, no need to do anything further
                if (initial == null || !(initial instanceof RecordingCollection<?, ?>) && ((Collection<?>) initial).isEmpty()) {
                    return null;
                }
                // Skip doing anything if the collections kept being empty
                if (current instanceof RecordingCollection<?, ?> && !((RecordingCollection<?, ?>) current).hasActions() && ((RecordingCollection<?, ?>) current).isEmpty()) {
                    return null;
                }
                if (elementDescriptor.shouldFlushMutations()) {
                    if (elementDescriptor.supportsDirtyCheck()) {
                        if (current instanceof RecordingCollection<?, ?>) {
                            return getDirtyFlusherForRecordingCollection(context, (V) initial, (RecordingCollection<?, ?>) current);
                        } else {
                            // Since we don't know what changed in the collection, we do a full fetch and merge
                            return this;
                        }
                    } else if (elementDescriptor.supportsDeepEqualityCheck() || elementDescriptor.isJpaEntity()) {
                        // If we can determine equality, we fetch and merge the elements
                        // We also fetch if we have entities since we assume collection rarely change drastically
                        if (current instanceof RecordingCollection<?, ?>) {
                            List<? extends CollectionAction<?>> actions = ((RecordingCollection<?, ?>) current).resetActions(context);
                            if (elementDescriptor.isIdentifiable()) {
                                return partialFlusher(true, PluralFlushOperation.COLLECTION_REPLAY_AND_ELEMENT, actions, getElementFlushers(context, (V) current, actions));
                            }
                        }

                        return this;
                    } else {
                        // Always reset the actions as that indicates changes
                        if (current instanceof RecordingCollection<?, ?>) {
                            ((RecordingCollection<?, ?>) current).resetActions(context);
                        }
                        // Other types are mutable basic types that aren't known to us like e.g. java.util.Date would be if we hadn't registered it
                        return partialFlusher(false, PluralFlushOperation.COLLECTION_REPLACE_ONLY, Collections.EMPTY_LIST, Collections.<CollectionElementAttributeFlusher<E, V>>emptyList());
                    }
                } else {
                    // Immutable elements in an updatable collection
                    if (current instanceof RecordingCollection<?, ?>) {
                        return getDirtyFlusherForRecordingCollection(context, (V) initial, (RecordingCollection<?, ?>) current);
                    } else {
                        // Since we don't know what changed in the collection, we do a full fetch and merge
                        return this;
                    }
                }
            }
        } else {
            // Not updatable
            if (elementDescriptor.shouldFlushMutations()) {
                if (initial != current) {
                    // If the reference changed, this is probably because of defensive copies
                    return null;
                } else {
                    // Flushes for non-identifiable types can't be done separately, so we need to fetch and merge accordingly
                    if (!elementDescriptor.isIdentifiable()) {
                        return this;
                    }
                    return getElementOnlyFlusher(context, (V) current);
                }
            } else {
                // Not updatable and no cascading, this is for pass through flushers only
                return null;
            }
        }
    }

    @Override
    protected CollectionElementAttributeFlusher<E, V> createPersistFlusher(TypeDescriptor typeDescriptor, Object element) {
        return new PersistCollectionElementAttributeFlusher<E, V>(element, optimisticLockProtected);
    }

    @Override
    protected CollectionElementAttributeFlusher<E, V> createMergeFlusher(TypeDescriptor typeDescriptor, Object element) {
        return new MergeCollectionElementAttributeFlusher<E, V>(element, optimisticLockProtected);
    }

    protected DirtyAttributeFlusher<CollectionAttributeFlusher<E, V>, E, V> determineDirtyFlusherForNewCollection(UpdateContext context, V initial, V current) {
        EqualityChecker equalityChecker;
        if (elementDescriptor.isSubview()) {
            equalityChecker = EqualsEqualityChecker.INSTANCE;
        } else {
            equalityChecker = new IdentityEqualityChecker(elementDescriptor.getBasicUserType());
        }
        List<CollectionAction<Collection<?>>> collectionActions = determineCollectionActions(context, initial, current, equalityChecker);

        // If nothing changed in the collection and no changes should be flushed, we are done
        if (collectionActions.size() == 0) {
            List<CollectionElementAttributeFlusher<E, V>> elementFlushers = getElementFlushers(context, current, collectionActions);
            // Always reset the actions as that indicates changes
            if (current instanceof RecordingCollection<?, ?>) {
                ((RecordingCollection<?, ?>) current).resetActions(context);
            }
            // A "null" element flusher list is given when a fetch and compare is more appropriate
            if (elementFlushers == null) {
                return this;
            }
            return getReplayAndElementFlusher(context, initial, current, collectionActions, elementFlushers);
        }

        if (inverseFlusher != null) {
            Map<Object, Object>[] addedAndRemoved;
            // Always reset the actions as that indicates changes
            if (current instanceof RecordingCollection<?, ?>) {
                addedAndRemoved = getAddedAndRemovedElementsForInverseFlusher((RecordingCollection<?, ?>) current, context);
            } else {
                addedAndRemoved = getAddedAndRemovedElementsForInverseFlusher(current, collectionActions);
            }
            // Inverse collections must convert collection actions to element flush actions
            Map<Object, Object> added = addedAndRemoved[0];
            Map<Object, Object> removed = addedAndRemoved[1];
            List<CollectionElementAttributeFlusher<E, V>> elementFlushers = getInverseElementFlushersForActions(context, current, added, removed);
            return partialFlusher(false, PluralFlushOperation.ELEMENT_ONLY, Collections.EMPTY_LIST, elementFlushers);
        }

        if (collectionActions.size() > current.size()) {
            // Always reset the actions as that indicates changes
            if (current instanceof RecordingCollection<?, ?>) {
                ((RecordingCollection<?, ?>) current).resetActions(context);
            }
            // More collection actions means more statements are issued
            // We'd rather replace in such a case
            if (elementDescriptor.shouldFlushMutations()) {
                return getReplaceOrMergeAndElementFlusher(context, initial, current);
            } else {
                return getReplaceOrMergeOnlyFlusher(context, initial, current);
            }
        } else {
            // Reset the actions since we determined new actions
            if (current instanceof RecordingCollection<?, ?>) {
                RecordingCollection<Collection<?>, ?> recordingCollection = (RecordingCollection<Collection<?>, ?>) current;
                recordingCollection.initiateActionsAgainstState(collectionActions, initial);
            }
            // If we determine possible collection actions, we try to apply them, but if not
            if (elementDescriptor.shouldFlushMutations()) {
                List<CollectionElementAttributeFlusher<E, V>> elementFlushers = getElementFlushers(context, current, collectionActions);
                // A "null" element flusher list is given when a fetch and compare is more appropriate
                if (elementFlushers == null) {
                    return this;
                }
                if (current instanceof RecordingCollection<?, ?>) {
                    ((RecordingCollection<?, ?>) current).resetActions(context);
                }
                return getReplayAndElementFlusher(context, initial, current, collectionActions, elementFlushers);
            } else {
                if (current instanceof RecordingCollection<?, ?>) {
                    ((RecordingCollection<?, ?>) current).resetActions(context);
                }
                return getReplayOnlyFlusher(context, initial, current, collectionActions);
            }
        }
    }

    protected List<CollectionAction<Collection<?>>> determineJpaCollectionActions(UpdateContext context, V initial, V current, EqualityChecker equalityChecker) {
        // We try to find a common prefix and from that on, we infer actions
        List<CollectionAction<Collection<?>>> actions = new ArrayList<>();
        Object[] objectsToAdd = current.toArray();
        final CollectionRemoveAllAction removeAllAction = new CollectionRemoveAllAction<>(0, collectionInstantiator.allowsDuplicates());
        int addSize = objectsToAdd.length;

        OUTER: for (Object initialObject : initial) {
            for (int i = 0; i < objectsToAdd.length; i++) {
                Object currentObject = objectsToAdd[i];
                if (currentObject != REMOVED_MARKER) {
                    if (equalityChecker.isEqual(context, initialObject, currentObject)) {
                        objectsToAdd[i] = REMOVED_MARKER;
                        addSize--;
                        continue OUTER;
                    }
                }
            }
            removeAllAction.add(initialObject);
        }

        if (!removeAllAction.isEmpty()) {
            actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) removeAllAction);
        }

        addAddAction(actions, objectsToAdd, addSize);

        return actions;
    }

    protected final List<CollectionAction<Collection<?>>> determineCollectionActionsForSubview(UpdateContext context, V initial, V current) {
        // We try to find a common prefix and from that on, we infer actions
        List<CollectionAction<Collection<?>>> actions = new ArrayList<>();
        Object[] objectsToAdd = current.toArray();
        int addSize = objectsToAdd.length;

        if (initial != null && !initial.isEmpty()) {
            final AttributeAccessor subviewIdAccessor = elementDescriptor.getViewToEntityMapper().getViewIdAccessor();
            final CollectionRemoveAllAction removeAllAction = new CollectionRemoveAllAction<>(0, collectionInstantiator.allowsDuplicates());

            OUTER:
            for (Object initialObject : initial) {
                Object initialViewId = subviewIdAccessor.getValue(initialObject);
                for (int i = 0; i < objectsToAdd.length; i++) {
                    Object currentObject = objectsToAdd[i];
                    if (currentObject != REMOVED_MARKER) {
                        Object currentViewId = subviewIdAccessor.getValue(currentObject);
                        if (initialViewId.equals(currentViewId)) {
                            objectsToAdd[i] = REMOVED_MARKER;
                            addSize--;
                            continue OUTER;
                        }
                    }
                }
                removeAllAction.add(initialObject);
            }

            if (!removeAllAction.isEmpty()) {
                actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) removeAllAction);
            }
        }

        addAddAction(actions, objectsToAdd, addSize);

        return actions;
    }

    protected final List<CollectionAction<Collection<?>>> determineCollectionActionsForNonSubview(UpdateContext context, V initial, V current, EqualityChecker equalityChecker) {
        // We try to find a common prefix and from that on, we infer actions
        List<CollectionAction<Collection<?>>> actions = new ArrayList<>();
        Object[] objectsToAdd = current.toArray();
        int addSize = objectsToAdd.length;

        if (initial != null && !initial.isEmpty()) {
            final CollectionRemoveAllAction removeAllAction = new CollectionRemoveAllAction<>(0, collectionInstantiator.allowsDuplicates());

            OUTER:
            for (Object initialObject : initial) {
                for (int i = 0; i < objectsToAdd.length; i++) {
                    Object currentObject = objectsToAdd[i];
                    if (currentObject != REMOVED_MARKER) {
                        if (equalityChecker.isEqual(context, initialObject, currentObject)) {
                            objectsToAdd[i] = REMOVED_MARKER;
                            addSize--;
                            continue OUTER;
                        }
                    }
                }
                removeAllAction.add(initialObject);
            }

            if (!removeAllAction.isEmpty()) {
                actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) removeAllAction);
            }
        }

        addAddAction(actions, objectsToAdd, addSize);

        return actions;
    }

    private void addAddAction(List<CollectionAction<Collection<?>>> actions, Object[] objectsToAdd, int addSize) {
        if (addSize != 0) {
            CollectionAddAllAction addAllAction = new CollectionAddAllAction<>(addSize, collectionInstantiator.allowsDuplicates());
            for (int i = 0; i < objectsToAdd.length; i++) {
                Object currentObject = objectsToAdd[i];
                if (currentObject != REMOVED_MARKER) {
                    addAllAction.add(currentObject);
                }
            }

            actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) addAllAction);
        }
    }

    protected List<CollectionAction<Collection<?>>> determineCollectionActions(UpdateContext context, V initial, V current, EqualityChecker equalityChecker) {
        if (elementDescriptor.isSubview() && elementDescriptor.isIdentifiable()) {
            return determineCollectionActionsForSubview(context, initial, current);
        } else {
            return determineCollectionActionsForNonSubview(context, initial, current, equalityChecker);
        }
    }

    @Override
    protected List<CollectionElementAttributeFlusher<E, V>> getElementFlushers(UpdateContext context, V current, List<? extends CollectionAction<?>> actions) {
        List<CollectionElementAttributeFlusher<E, V>> elementFlushers = new ArrayList<>();
        if (determineElementFlushers(context, elementDescriptor, elementFlushers, current, actions, current)) {
            return null;
        }

        return elementFlushers;
    }

    protected CollectionAttributeFlusher<E, V> partialFlusher(boolean fetch, PluralFlushOperation operation, List<? extends CollectionAction<?>> collectionActions, List<CollectionElementAttributeFlusher<E, V>> elementFlushers) {
        return new CollectionAttributeFlusher<E, V>(this, fetch, operation, collectionActions, elementFlushers);
    }

    @Override
    protected boolean collectionEquals(V initial, V current) {
        if (initial.size() != current.size()) {
            return false;
        }

        return initial.containsAll(current);
    }

    protected boolean areActionsQueueable(RecordingCollection<?, ?> collection) {
        // Currently, no collection action has real queueing support in Hibernate
        return false;
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    interface ElementChangeListener<E, V> {

        void onAddedInverseElement(Object element);

        void onAddedAndUpdatedInverseElement(DirtyAttributeFlusher<?, E, V> flusher, Object element);

        void onUpdatedInverseElement(DirtyAttributeFlusher<?, E, V> flusher, Object element);

        void onRemovedInverseElement(Object element);
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private class ElementFlusherCollector implements ElementChangeListener<E, V> {
        final List<CollectionElementAttributeFlusher<E, V>> elementFlushers = new ArrayList<>();

        @Override
        public void onAddedInverseElement(Object element) {
            elementFlushers.add((CollectionElementAttributeFlusher<E, V>) (CollectionElementAttributeFlusher<?, ?>) new InverseCollectionElementAttributeFlusher<>(
                    null, element, optimisticLockProtected, inverseFlusher, InverseCollectionElementAttributeFlusher.Strategy.SET
            ));
        }

        @Override
        public void onAddedAndUpdatedInverseElement(DirtyAttributeFlusher<?, E, V> flusher, Object element) {
            elementFlushers.add((CollectionElementAttributeFlusher<E, V>) (CollectionElementAttributeFlusher<?, ?>) new InverseCollectionElementAttributeFlusher<>(
                    flusher, element, optimisticLockProtected, inverseFlusher, InverseCollectionElementAttributeFlusher.Strategy.SET
            ));
        }

        @Override
        public void onUpdatedInverseElement(DirtyAttributeFlusher<?, E, V> flusher, Object element) {
            elementFlushers.add(new UpdateCollectionElementAttributeFlusher<>(flusher, element, optimisticLockProtected, elementDescriptor.getViewToEntityMapper()));
        }

        @Override
        public void onRemovedInverseElement(Object element) {
            elementFlushers.add((CollectionElementAttributeFlusher<E, V>) (CollectionElementAttributeFlusher<?, ?>) new InverseCollectionElementAttributeFlusher<>(
                    null, element, optimisticLockProtected, inverseFlusher, inverseRemoveStrategy
            ));
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private class ElementFlusherEntityExecutor implements ElementChangeListener<E, V> {

        private final UpdateContext context;
        private final E entity;

        public ElementFlusherEntityExecutor(UpdateContext context, E entity) {
            this.context = context;
            this.entity = entity;
        }

        @Override
        public void onAddedInverseElement(Object element) {
            inverseFlusher.flushEntitySetElement(context, element, entity, entity, null);
        }

        @Override
        public void onAddedAndUpdatedInverseElement(DirtyAttributeFlusher<?, E, V> flusher, Object element) {
            inverseFlusher.flushEntitySetElement(context, element, entity, entity, (DirtyAttributeFlusher<?, E, Object>) (DirtyAttributeFlusher<?, ?, ?>) flusher);
        }

        @Override
        public void onUpdatedInverseElement(DirtyAttributeFlusher<?, E, V> flusher, Object element) {
            new UpdateCollectionElementAttributeFlusher<>(flusher, element, optimisticLockProtected, elementDescriptor.getViewToEntityMapper())
                    .flushEntity(context, null, null, null, null, null);
        }

        @Override
        public void onRemovedInverseElement(Object element) {
            if (inverseRemoveStrategy == InverseCollectionElementAttributeFlusher.Strategy.SET_NULL) {
                inverseFlusher.flushEntitySetElement(context, element, entity, null, null);
            } else if (inverseRemoveStrategy != InverseCollectionElementAttributeFlusher.Strategy.IGNORE) {
                // We need to remove the element from the entity backing collection as well, otherwise it might not be removed properly when using cascading
                // Note that this is only necessary for entity flushing which is handled by this code. JPA DML statements like use for query flushing don't respect cascading configurations
                Collection<Object> entityCollection = (Collection<Object>) entityAttributeMapper.getValue(entity);
                if (entityCollection != null) {
                    if (elementDescriptor.getViewToEntityMapper() == null) {
                        // Element is an entity object so just remove
                        entityCollection.remove(element);
                    } else {
                        final AttributeAccessor entityIdAccessor = elementDescriptor.getViewToEntityMapper().getEntityIdAccessor();
                        final AttributeAccessor subviewIdAccessor = elementDescriptor.getViewToEntityMapper().getViewIdAccessor();
                        final Object subviewId = subviewIdAccessor.getValue(element);
                        final Iterator iterator = entityCollection.iterator();
                        while (iterator.hasNext()) {
                            Object collectionElement = iterator.next();
                            Object elementId = entityIdAccessor.getValue(collectionElement);
                            if (elementId.equals(subviewId)) {
                                iterator.remove();
                                break;
                            }
                        }
                    }
                }
                inverseFlusher.removeElement(context, entity, element);
            }
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private class ElementFlusherQueryExecutor implements ElementChangeListener<E, V> {

        private final UpdateContext context;
        private final E entity;
        private final Object view;

        public ElementFlusherQueryExecutor(UpdateContext context, E entity, Object view) {
            this.context = context;
            this.entity = entity;
            this.view = view;
        }

        @Override
        public void onAddedInverseElement(Object element) {
            if (view != null) {
                inverseFlusher.flushQuerySetElement(context, element, view, view, null, null);
            } else {
                inverseFlusher.flushQuerySetEntityOnElement(context, element, entity, entity, null, null);
            }
        }

        @Override
        public void onAddedAndUpdatedInverseElement(DirtyAttributeFlusher<?, E, V> flusher, Object element) {
            if (view != null) {
                inverseFlusher.flushQuerySetElement(context, element, view, view, null, (DirtyAttributeFlusher<?, E, Object>) (DirtyAttributeFlusher<?, ?, ?>) flusher);
            } else {
                inverseFlusher.flushQuerySetEntityOnElement(context, element, entity, entity, null, (DirtyAttributeFlusher<?, E, Object>) (DirtyAttributeFlusher<?, ?, ?>) flusher);
            }
        }

        @Override
        public void onUpdatedInverseElement(DirtyAttributeFlusher<?, E, V> flusher, Object element) {
            new UpdateCollectionElementAttributeFlusher<>(flusher, element, optimisticLockProtected, elementDescriptor.getViewToEntityMapper())
                .flushQuery(context, null, null, null, null, null, null);
        }

        @Override
        public void onRemovedInverseElement(Object element) {
            if (inverseRemoveStrategy == InverseCollectionElementAttributeFlusher.Strategy.SET_NULL) {
                inverseFlusher.flushQuerySetElement(context, element, view, null, null, null);
            } else if (inverseRemoveStrategy != InverseCollectionElementAttributeFlusher.Strategy.IGNORE) {
                inverseFlusher.removeElement(context, entity, element);
            }
        }
    }

    private List<CollectionElementAttributeFlusher<E, V>> getInverseElementFlushersForActions(UpdateContext context, Iterable<?> current, Map<Object, Object> added, Map<Object, Object> removed) {
        ElementFlusherCollector listener = new ElementFlusherCollector();
        visitInverseElementFlushersForActions(context, current, added, removed, listener);
        return listener.elementFlushers;
    }

    private void visitInverseElementFlushersForActions(UpdateContext context, Iterable<?> current, Map<Object, Object> added, Map<Object, Object> removed, ElementChangeListener<E, V> listener) {
        if (elementDescriptor.isSubview()) {
            final ViewToEntityMapper mapper = elementDescriptor.getViewToEntityMapper();
            // First remove elements, then persist, otherwise we might get a constrain violation
            for (Object element : removed.values()) {
                listener.onRemovedInverseElement(element);
            }
            final Iterator<Object> iter = getRecordingIterator((V) current);
            try {
                while (iter.hasNext()) {
                    Object elem = iter.next();
                    if (elem instanceof MutableStateTrackable) {
                        MutableStateTrackable element = (MutableStateTrackable) elem;
                        @SuppressWarnings("unchecked")
                        DirtyAttributeFlusher<?, E, V> flusher = (DirtyAttributeFlusher<?, E, V>) (DirtyAttributeFlusher) mapper.getNestedDirtyFlusher(context, element, (DirtyAttributeFlusher) null);
                        if (flusher != null) {
                            Object addedElement = added.remove(element);
                            if (addedElement != null) {
                                listener.onAddedAndUpdatedInverseElement(flusher, element);
                            } else {
                                listener.onUpdatedInverseElement(flusher, element);
                            }
                        }
                    }
                }
            } finally {
                resetRecordingIterator((V) current);
            }
            // Non-dirty added values
            for (Object element : added.values()) {
                listener.onAddedInverseElement(element);
            }
        } else if (elementDescriptor.isJpaEntity()) {
            for (Object element : removed.values()) {
                listener.onRemovedInverseElement(element);
            }
            for (Object element : current) {
                if (elementDescriptor.getBasicUserType().shouldPersist(element) && elementDescriptor.shouldJpaPersist()) {
                    CollectionElementAttributeFlusher<E, V> flusher = new PersistCollectionElementAttributeFlusher<>(element, optimisticLockProtected);
                    Object addedElement = added.remove(element);
                    if (addedElement != null) {
                        listener.onAddedAndUpdatedInverseElement(flusher, element);
                    } else {
                        listener.onUpdatedInverseElement(flusher, element);
                    }
                } else if (elementDescriptor.shouldJpaMerge()) {
                    if (element != null) {
                        // Although we can't replace the original object in the backing collection, we don't care in case of inverse collections
                        CollectionElementAttributeFlusher<E, V> flusher = new MergeCollectionElementAttributeFlusher<>(element, optimisticLockProtected);
                        Object addedElement = added.remove(element);
                        if (addedElement != null) {
                            listener.onAddedAndUpdatedInverseElement(flusher, element);
                        } else {
                            listener.onUpdatedInverseElement(flusher, element);
                        }
                    }
                } else {
                    Object addedElement = added.remove(element);
                    if (addedElement != null) {
                        listener.onAddedInverseElement(element);
                    }
                }
            }
        } else {
            throw new UnsupportedOperationException("Not yet implemented!");
        }
    }

    @Override
    protected DirtyAttributeFlusher<CollectionAttributeFlusher<E, V>, E, V> getDirtyFlusherForRecordingCollection(UpdateContext context, V initial, RecordingCollection<?, ?> collection) {
        if (collection.hasActions()) {
            List<? extends CollectionAction<?>> actions = collection.resetActions(context);
            boolean queueable = areActionsQueueable(collection);

            if (queueable) {
                if (elementDescriptor.shouldFlushMutations()) {
                    // When no mapper is given, we have basic types so we need to fetch and merge accordingly
                    if (elementDescriptor.isBasic()) {
                        return this;
                    }
                    // Check elements for dirtyness
                    @SuppressWarnings("unchecked")
                    List<CollectionElementAttributeFlusher<E, V>> elementFlushers = getElementFlushers(context, (V) collection, actions);
                    // A "null" element flusher list is given when a fetch and compare is more appropriate
                    if (elementFlushers == null) {
                        return this;
                    }

                    int actionUnrelatedDirtyCount = getActionUnrelatedDirtyObjectCount(initial, elementFlushers, actions);

                    // At some point we might want to consider a threshold here instead
                    if (actionUnrelatedDirtyCount == 0) {
                        // If the dirty objects are the ones which are added/removed via collection actions, we don't need to load the collection
                        return partialFlusher(false, PluralFlushOperation.COLLECTION_REPLAY_AND_ELEMENT, actions, elementFlushers);
                    } else {
                        // If some objects are dirty that previously existed in the collection, we should load the collection
                        return partialFlusher(true, PluralFlushOperation.COLLECTION_REPLAY_AND_ELEMENT, actions, elementFlushers);
                    }
                } else {
                    // If the operations are queueable and elements should not receive update cascades, we don't need to load the collection
                    return partialFlusher(false, PluralFlushOperation.COLLECTION_REPLAY_ONLY, actions, Collections.<CollectionElementAttributeFlusher<E, V>>emptyList());
                }
            } else if (inverseFlusher != null) {
                // Inverse collections must convert collection actions to element flush actions
                Map<Object, Object>[] addedAndRemoved = getAddedAndRemovedElementsForInverseFlusher(actions);
                Map<Object, Object> added = addedAndRemoved[0];
                Map<Object, Object> removed = addedAndRemoved[1];
                List<CollectionElementAttributeFlusher<E, V>> elementFlushers = getInverseElementFlushersForActions(context, collection, added, removed);
                return partialFlusher(false, PluralFlushOperation.ELEMENT_ONLY, Collections.EMPTY_LIST, elementFlushers);
            } else {
                // If the operations aren't queueable, we always need to load the collection
                if (elementDescriptor.shouldFlushMutations()) {
                    // When no mapper is given, we have basic types so we need to fetch and merge accordingly
                    if (elementDescriptor.isBasic()) {
                        return this;
                    }
                    List<CollectionElementAttributeFlusher<E, V>> elementFlushers = getElementFlushers(context, (V) collection, actions);
                    // A "null" element flusher list is given when a fetch and compare is more appropriate
                    if (elementFlushers == null) {
                        return this;
                    }
                    return getReplayAndElementFlusher(context, initial, (V) collection, actions, elementFlushers);
                } else {
                    return getReplayOnlyFlusher(context, initial, (V) collection, actions);
                }
            }
        }

        // If the elements are mutable, we always have to check the collection, so we load and compute diffs
        if (elementDescriptor.shouldFlushMutations()) {
            // When no mapper is given, we have basic types so we need to fetch and merge accordingly
            if (elementDescriptor.isBasic()) {
                return this;
            }
            return getElementOnlyFlusher(context, (V) collection);
        }

        // No outstanding actions and elements are not mutable, so we are done here
        return null;
    }

    @SuppressWarnings("unchecked")
    protected FusedCollectionActions getFusedOperations(List<? extends CollectionAction<?>> collectionActions) {
        if (collectionInstantiator.allowsDuplicates()) {
            // Don't support bags
            return null;
        }

        Map<Object, Object> added = new IdentityHashMap<>();
        Map<Object, Object> removed = new IdentityHashMap<>();
        for (CollectionAction<? extends Collection<?>> a : collectionActions) {
            Collection<Object> addedObjects = a.getAddedObjects();
            Collection<Object> removedObjects = a.getRemovedObjects();

            for (Object addedObject : addedObjects) {
                removed.remove(addedObject);
            }
            for (Object removedObject : removedObjects) {
                added.remove(removedObject);
                removed.put(removedObject, removedObject);
            }
            for (Object addedObject : addedObjects) {
                added.put(addedObject, addedObject);
            }
        }
        return new FusedCollectionElementActions(elementDescriptor.getViewToEntityMapper() == null ? null : elementDescriptor.getLoadOnlyViewToEntityMapper(), removed, added);
    }

    @SuppressWarnings("unchecked")
    private Map<Object, Object>[] getAddedAndRemovedElementsForInverseFlusher(RecordingCollection<?, ?> collection, UpdateContext context) {
        List<? extends CollectionAction<?>> collectionActions = collection.resetActions(context);
        return getAddedAndRemovedElementsForInverseFlusher(collectionActions);
    }

    @SuppressWarnings("unchecked")
    private Map<Object, Object>[] getAddedAndRemovedElementsForInverseFlusher(List<? extends CollectionAction<?>> collectionActions) {
        Map<Object, Object> added = new IdentityHashMap<>();
        Map<Object, Object> removed = new IdentityHashMap<>();
        for (CollectionAction<? extends Collection<?>> a : collectionActions) {
            Collection<Object> addedObjects = a.getAddedObjects();
            Collection<Object> removedObjects = a.getRemovedObjects();

            for (Object addedObject : addedObjects) {
                removed.remove(addedObject);
            }
            for (Object removedObject : removedObjects) {
                added.remove(removedObject);
                removed.put(removedObject, removedObject);
            }
            for (Object addedObject : addedObjects) {
                added.put(addedObject, addedObject);
            }
        }
        return new Map[]{ added, removed };
    }

    @SuppressWarnings("unchecked")
    private Map<Object, Object>[] getAddedAndRemovedElementsForInverseFlusher(Collection<?> collection, List<CollectionAction<Collection<?>>> collectionActions) {
        Map<Object, Object> added = new IdentityHashMap<>();
        Map<Object, Object> removed = new IdentityHashMap<>();
        for (CollectionAction<Collection<?>> a : collectionActions) {
            Collection<Object> addedObjects = a.getAddedObjects(collection);
            Collection<Object> removedObjects = a.getRemovedObjects(collection);

            for (Object addedObject : addedObjects) {
                removed.remove(addedObject);
            }
            for (Object removedObject : removedObjects) {
                added.remove(removedObject);
                removed.put(removedObject, removedObject);
            }
            for (Object addedObject : addedObjects) {
                added.put(addedObject, addedObject);
            }
        }
        return new Map[]{ added, removed };
    }

    // Determines how many objects are dirty, ignoring the ones that are added/removed via actions
    protected final int getActionUnrelatedDirtyObjectCount(V initial, List<CollectionElementAttributeFlusher<E, V>> elementFlushers, List<? extends CollectionAction<?>> actions) {
        int count = 0;
        for (CollectionElementAttributeFlusher<E, V> flusherEntry : elementFlushers) {
            Object objectToFlush = flusherEntry.getElement();
            for (CollectionAction<V> a : (List<? extends CollectionAction<V>>) (List<?>) actions) {
                if (a.containsObject(initial, objectToFlush)) {
                    count++;
                    break;
                }
            }
        }

        return count;
    }

}
