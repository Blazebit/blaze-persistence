/*
 * Copyright 2014 - 2023 Blazebit.
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
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.UpdateCriteriaBuilder;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.accessor.InitialValueAttributeAccessor;
import com.blazebit.persistence.view.impl.collection.CollectionAction;
import com.blazebit.persistence.view.impl.collection.CollectionClearAction;
import com.blazebit.persistence.view.impl.collection.CollectionInstantiatorImplementor;
import com.blazebit.persistence.view.impl.collection.ListAction;
import com.blazebit.persistence.view.impl.collection.ListAddAction;
import com.blazebit.persistence.view.impl.collection.ListAddAllAction;
import com.blazebit.persistence.view.impl.collection.ListRemoveAction;
import com.blazebit.persistence.view.impl.collection.ListSetAction;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.collection.RecordingList;
import com.blazebit.persistence.view.impl.collection.CollectionRemoveListener;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.spi.type.DirtyStateTrackable;
import com.blazebit.persistence.view.spi.type.MutableStateTrackable;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.spi.type.BasicUserType;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class IndexedListAttributeFlusher<E, V extends List<?>> extends CollectionAttributeFlusher<E, V> {

    @SuppressWarnings("unchecked")
    public IndexedListAttributeFlusher(String attributeName, String mapping, Class<?> ownerEntityClass, String ownerIdAttributeName, String ownerMapping, DirtyAttributeFlusher<?, ?, ?> ownerIdFlusher, DirtyAttributeFlusher<?, ?, ?> elementFlusher, boolean supportsCollectionDml, FlushStrategy flushStrategy, AttributeAccessor attributeMapper, InitialValueAttributeAccessor viewAttributeAccessor,
                                       boolean optimisticLockProtected, boolean collectionUpdatable, boolean viewOnlyDeleteCascaded, boolean jpaProviderDeletesCollection, CollectionRemoveListener cascadeDeleteListener, CollectionRemoveListener removeListener, CollectionInstantiatorImplementor<?, ?> collectionInstantiator, TypeDescriptor elementDescriptor, InverseFlusher<E> inverseFlusher,
                                       InverseRemoveStrategy inverseRemoveStrategy) {
        super(attributeName, mapping, ownerEntityClass, ownerIdAttributeName, ownerMapping, ownerIdFlusher, elementFlusher, supportsCollectionDml, flushStrategy, attributeMapper, viewAttributeAccessor, optimisticLockProtected, collectionUpdatable, viewOnlyDeleteCascaded, jpaProviderDeletesCollection, cascadeDeleteListener, removeListener, collectionInstantiator, elementDescriptor,
                inverseFlusher, inverseRemoveStrategy);
    }

    public IndexedListAttributeFlusher(IndexedListAttributeFlusher<E, V> original, boolean fetch) {
        super(original, fetch);
    }

    public IndexedListAttributeFlusher(IndexedListAttributeFlusher<E, V> original, boolean fetch, PluralFlushOperation flushOperation, List<? extends CollectionAction<?>> collectionActions, List<CollectionElementAttributeFlusher<E, V>> elementFlushers) {
        super(original, fetch, flushOperation, collectionActions, elementFlushers);
    }

    @Override
    protected boolean collectionEquals(V initial, V current) {
        if (initial == null || initial.size() != current.size()) {
            return false;
        }

        return initial.equals(current);
    }

    @Override
    protected boolean mergeAndRequeue(UpdateContext context, RecordingCollection recordingCollection, Collection<Object> newCollection) {
        EntityManager em = context.getEntityManager();
        List<Object> realCollection = (List<Object>) newCollection;
        for (int i = 0; i < realCollection.size(); i++) {
            Object elem = realCollection.get(i);
            Object merged = persistOrMerge(em, elem);
            if (elem != merged) {
                if (recordingCollection != null) {
                    recordingCollection.replaceActionElement(elem, merged);
                }
                realCollection.set(i, merged);
            }
        }
        return true;
    }

    @Override
    protected CollectionAttributeFlusher<E, V> partialFlusher(boolean fetch, PluralFlushOperation operation, List<? extends CollectionAction<?>> collectionActions, List<CollectionElementAttributeFlusher<E, V>> elementFlushers) {
        return new IndexedListAttributeFlusher<>(this, fetch, operation, collectionActions, elementFlushers);
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
            if (current instanceof RecordingList<?>) {
                return super.getDirtyKind(initial, current);
            }
        } else {
            if (initial.size() != current.size()) {
                return DirtyKind.MUTATED;
            }
            if (elementDescriptor.shouldFlushMutations()) {
                if (elementDescriptor.supportsDirtyCheck()) {
                    if (elementDescriptor.isSubview()) {
                        ViewToEntityMapper mapper = elementDescriptor.getViewToEntityMapper();
                        for (int i = 0; i < current.size(); i++) {
                            Object o = current.get(i);
                            if (!Objects.equals(initial.get(i), o)) {
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
                        for (int i = 0; i < current.size(); i++) {
                            Object o = current.get(i);
                            if (!Objects.equals(initial.get(i), o)) {
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
    protected List<CollectionAction<Collection<?>>> replaceActions(V value) {
        List<CollectionAction<Collection<?>>> actions = new ArrayList<>();
        actions.add(new CollectionClearAction());
        if (value != null && !value.isEmpty()) {
            actions.add(new ListAddAllAction(0, true, value));
        }
        return actions;
    }

    @Override
    protected Collection<Object> appendRemoveSpecific(UpdateContext context, DeleteCriteriaBuilder<?> deleteCb, FusedCollectionActions fusedCollectionActions) {
        deleteCb.where("INDEX(e." + getMapping() + ")").in(fusedCollectionActions.getRemoved(context));
        return fusedCollectionActions.getRemoved();
    }

    @Override
    protected void addElements(UpdateContext context, Object ownerView, Object view, Collection<Object> removedAllObjects, boolean flushAtOnce, boolean removedAllWithoutCollectionActions, V value, List<Object> embeddablesToUpdate, FusedCollectionActions fusedCollectionActions, boolean initialKnown) {
        Collection<Object> appends;
        int appendIndex;
        String mapping = getMapping();
        if (fusedCollectionActions == null || !removedAllObjects.isEmpty()) {
            appends = (Collection<Object>) value;
            removedAllObjects.removeAll(appends);
            appendIndex = 0;
        } else {
            FusedCollectionIndexActions indexActions = (FusedCollectionIndexActions) fusedCollectionActions;
            List<FusedCollectionIndexActions.IndexTranslateOperation> translations = indexActions.getTranslations();
            if (translations.size() != 0) {
                UpdateCriteriaBuilder<?> updateCb = context.getEntityViewManager().getCriteriaBuilderFactory().updateCollection(context.getEntityManager(), ownerEntityClass, "e", mapping);
                updateCb.setExpression("INDEX(" + mapping + ")", "INDEX(" + mapping + ") + :offset");
                updateCb.setWhereExpression(ownerIdWhereFragment);
                updateCb.where("INDEX(" + mapping + ")").geExpression(":minIdx");
                updateCb.where("INDEX(" + mapping + ")").ltExpression(":maxIdx");
                Query query = updateCb.getQuery();
                ownerIdFlusher.flushQuery(context, null, null, query, ownerView, view, ownerIdFlusher.getViewAttributeAccessor().getValue(ownerView), null, null);
                for (int i = 0; i < translations.size(); i++) {
                    FusedCollectionIndexActions.IndexTranslateOperation translation = translations.get(i);
                    query.setParameter("minIdx", translation.getStartIndex());
                    query.setParameter("maxIdx", translation.getEndIndex());
                    query.setParameter("offset", translation.getOffset());
                    query.executeUpdate();
                }
            }

            List<FusedCollectionIndexActions.ReplaceOperation> replaces = indexActions.getReplaces();
            if (replaces.size() != 0 || embeddablesToUpdate != null && !embeddablesToUpdate.isEmpty()) {
                UpdateCriteriaBuilder<?> updateCb = context.getEntityViewManager().getCriteriaBuilderFactory().updateCollection(context.getEntityManager(), ownerEntityClass, "e", mapping);
                updateCb.setExpression(mapping, ":element");
                updateCb.setWhereExpression(ownerIdWhereFragment);
                updateCb.where("INDEX(" + mapping + ")").eqExpression(":idx");
                Query query = updateCb.getQuery();

                if (replaces.size() != 0) {
                    ownerIdFlusher.flushQuery(context, null, null, query, ownerView, view, ownerIdFlusher.getViewAttributeAccessor().getValue(ownerView), null, null);
                    boolean checkTransient = elementDescriptor.isJpaEntity() && !elementDescriptor.shouldJpaPersist();
                    if (elementDescriptor.getViewToEntityMapper() == null) {
                        for (int i = 0; i < replaces.size(); i++) {
                            FusedCollectionIndexActions.ReplaceOperation replace = replaces.get(i);
                            if (checkTransient && elementDescriptor.getBasicUserType().shouldPersist(replace.getNewObject())) {
                                throw new IllegalStateException("Collection " + attributeName + " references an unsaved transient instance - save the transient instance before flushing: " + replace.getNewObject());
                            }
                            query.setParameter("idx", replace.getIndex());
                            query.setParameter("element", replace.getNewObject());
                            query.executeUpdate();
                        }
                    } else {
                        ViewToEntityMapper loadOnlyViewToEntityMapper = elementDescriptor.getLoadOnlyViewToEntityMapper();
                        for (int i = 0; i < replaces.size(); i++) {
                            FusedCollectionIndexActions.ReplaceOperation replace = replaces.get(i);
                            query.setParameter("idx", replace.getIndex());
                            query.setParameter("element", loadOnlyViewToEntityMapper.applyToEntity(context, null, replace.getNewObject()));
                            query.executeUpdate();
                        }
                    }
                }
                if (embeddablesToUpdate != null && !embeddablesToUpdate.isEmpty()) {
                    for (int i = 0; i < embeddablesToUpdate.size(); i++) {
                        query.setParameter("idx", i);
                        query.setParameter("element", embeddablesToUpdate.get(i));
                        query.executeUpdate();
                    }
                }
            }

            appends = indexActions.getAdded(context);
            appendIndex = indexActions.getAppendIndex();
            removedAllObjects.removeAll(indexActions.getAdded());
        }

        if (appends.size() > 1 || appends.size() == 1 && appends.iterator().next() != null) {
            InsertCriteriaBuilder<?> insertCb = context.getEntityViewManager().getCriteriaBuilderFactory().insertCollection(context.getEntityManager(), ownerEntityClass, mapping);

            String entityIdAttributeName = elementDescriptor.getEntityIdAttributeName();
            String attributeIdAttributeName = elementDescriptor.getAttributeIdAttributeName();
            if (entityIdAttributeName == null) {
                insertCb.fromValues(ownerEntityClass, mapping, "val", 1);
            } else if (entityIdAttributeName.equals(attributeIdAttributeName)) {
                insertCb.fromIdentifiableValues((Class<Object>) elementDescriptor.getJpaType(), "val", 1);
            } else {
                insertCb.fromIdentifiableValues((Class<Object>) elementDescriptor.getJpaType(), attributeIdAttributeName, "val", 1);
            }
            if (initialKnown) {
                insertCb.bind("INDEX(" + mapping + ")").select("FUNCTION('TREAT_INTEGER', :idx)");
            } else {
                SubqueryBuilder<? extends InsertCriteriaBuilder<?>> subquery = insertCb.bind("INDEX(" + mapping + ")")
                        .selectSubquery("subquery", "COALESCE(subquery + 1, 0)")
                        .from(ownerEntityClass, "sub")
                        .select("MAX(INDEX(sub." + mapping + "))");
                for (int i = 0; i < ownerIdBindFragments.length; i += 2) {
                    subquery.where("sub." + ownerIdBindFragments[i]).eqExpression(ownerIdBindFragments[i + 1]);
                }
                subquery.end();
            }
            for (int i = 0; i < ownerIdBindFragments.length; i += 2) {
                insertCb.bind(ownerIdBindFragments[i]).select(ownerIdBindFragments[i + 1]);
            }
            insertCb.bind(mapping).select("val");
            Query query = insertCb.getQuery();
            ownerIdFlusher.flushQuery(context, null, null, query, ownerView, view, ownerIdFlusher.getViewAttributeAccessor().getValue(ownerView), null, null);

            // TODO: Use batching when we implement #657
            Object[] singletonArray = new Object[1];
            List<Object> singletonList = Arrays.asList(singletonArray);
            if (elementDescriptor.getViewToEntityMapper() == null) {
                boolean checkTransient = elementDescriptor.isJpaEntity() && !elementDescriptor.shouldJpaPersist();
                for (Object object : appends) {
                    if (object != null) {
                        if (checkTransient && elementDescriptor.getBasicUserType().shouldPersist(object)) {
                            throw new IllegalStateException("Collection " + attributeName + " references an unsaved transient instance - save the transient instance before flushing: " + object);
                        }
                        singletonArray[0] = object;
                        if (initialKnown) {
                            query.setParameter("idx", appendIndex++);
                        }
                        query.setParameter("val", singletonList);
                        query.executeUpdate();
                    }
                }
            } else {
                ViewToEntityMapper loadOnlyViewToEntityMapper = elementDescriptor.getLoadOnlyViewToEntityMapper();
                for (Object object : appends) {
                    if (object != null) {
                        singletonArray[0] = loadOnlyViewToEntityMapper.applyToEntity(context, null, object);
                        if (initialKnown) {
                            query.setParameter("idx", appendIndex++);
                        }
                        query.setParameter("val", singletonList);
                        query.executeUpdate();
                    }
                }
            }
        }
    }

    @Override
    protected boolean canFlushSeparateCollectionOperations() {
        return true;
    }

    @Override
    protected boolean isIndexed() {
        return true;
    }

    @Override
    protected void addFlatViewElementFlushActions(UpdateContext context, TypeDescriptor typeDescriptor, List<CollectionAction<?>> actions, V current) {
        final ViewToEntityMapper mapper = typeDescriptor.getViewToEntityMapper();
        for (int i = 0; i < current.size(); i++) {
            Object o = current.get(i);
            if (o instanceof MutableStateTrackable) {
                MutableStateTrackable element = (MutableStateTrackable) o;
                @SuppressWarnings("unchecked")
                DirtyAttributeFlusher<?, E, V> flusher = (DirtyAttributeFlusher<?, E, V>) (DirtyAttributeFlusher) mapper.getNestedDirtyFlusher(context, element, (DirtyAttributeFlusher) null);
                if (flusher != null) {
                    // At this point, we have to check the collection actions to determine if the view was added through actions somehow
                    // We will register a special ListSetAction if the view was not added through actions to issue an UPDATE statement
                    // By default, since the view is dirty, we start with the state UPDATED and go through state transitions
                    // based on the containment of the view in the added/removed objects collections of the actions
                    EntryState state = EntryState.UPDATED;
                    Object replacedObject = element;
                    for (CollectionAction<?> action : actions) {
                        Collection<Object> removedObjects = action.getRemovedObjects();
                        if (identityContains(removedObjects, element)) {
                            if (identityContains(action.getAddedObjects(), element)) {
                                // This is a ListSetAction where the old and new object are the same instances
                                replacedObject = element;
                                state = EntryState.UPDATED;
                            } else {
                                state = state.onRemove();
                            }
                        } else if (identityContains(action.getAddedObjects(), element)) {
                            if (removedObjects.isEmpty()) {
                                state = state.onAdd();
                            } else {
                                // This is a ListSetAction which has only a single element, so this is safe
                                replacedObject = removedObjects.iterator().next();
                                state = EntryState.UPDATED;
                            }
                        }
                    }

                    // If the element was UPDATED and there is no replacedObject,
                    // this means that this really was just a mutation of the view
                    // and there is no action that would flush the object changes already
                    if (state == EntryState.UPDATED && replacedObject == element) {
                        // Using last = false is intentional to actually get a proper update instead of a delete and insert
                        actions.add(new ListSetAction<>(i, false, element, element));
                    }
                }
            }
        }
    }

    private int getInitialSize(int size, List<CollectionAction<?>> actions) {
        int end = actions.size() - 1;
        for (; end >= 0; end--) {
            CollectionAction<?> collectionAction = actions.get(end);
            size += collectionAction.getAddedObjects().size() - collectionAction.getRemovedObjects().size();
        }
        return size;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected FusedCollectionActions getFusedOperations(List<? extends CollectionAction<?>> collectionActions) {
        return new FusedCollectionIndexActions((List<? extends ListAction<?>>) (List) collectionActions);
    }

    @Override
    protected List<CollectionAction<Collection<?>>> determineJpaCollectionActions(UpdateContext context, V jpaCollection, V value, EqualityChecker equalityChecker) {
        // We try to find a common prefix and from that on, we infer actions
        List<CollectionAction<Collection<?>>> actions = new ArrayList<>();
        int jpaSize = jpaCollection.size();
        int lastUnmatchedIndex = 0;

        // If there is no mapper, the view element type is either basic or JPA managed
        // No need for id extraction in that case
        for (int i = 0; i < jpaSize; i++) {
            Object jpaElement = jpaCollection.get(i);
            if (i < value.size()) {
                Object viewElement = value.get(i);
                if (!equalityChecker.isEqual(context, jpaElement, viewElement)) {
                    break;
                } else {
                    lastUnmatchedIndex++;
                }
            } else {
                // JPA element was removed, remove all following elements and Keep the same index 'i'
                if (i < jpaSize) {
                    for (int j = i; j < jpaSize - 1; j++) {
                        actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) new ListRemoveAction<>(i, false, getViewElement(context, elementDescriptor, jpaCollection.get(i))));
                    }
                    actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) new ListRemoveAction<>(i, true, getViewElement(context, elementDescriptor, jpaCollection.get(i))));
                }

                // Break since there are no more elements to check
                lastUnmatchedIndex = jpaSize;
                break;
            }
        }
        // Remove remaining elements in the list that couldn't be matched
        if (lastUnmatchedIndex < jpaSize) {
            for (int i = lastUnmatchedIndex; i < jpaSize - 1; i++) {
                actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) new ListRemoveAction<>(lastUnmatchedIndex, false, getViewElement(context, elementDescriptor, jpaCollection.get(lastUnmatchedIndex))));
            }
            actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) new ListRemoveAction<>(lastUnmatchedIndex, true, getViewElement(context, elementDescriptor, jpaCollection.get(lastUnmatchedIndex))));
        }
        // Add new elements that are not matched
        if (lastUnmatchedIndex < value.size()) {
            for (int i = lastUnmatchedIndex; i < value.size(); i++) {
                actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) new ListAddAction<>(lastUnmatchedIndex, true, value.get(i)));
            }
        }

        return actions;
    }

    @Override
    protected List<CollectionAction<Collection<?>>> determineCollectionActions(UpdateContext context, V initial, V current, EqualityChecker equalityChecker) {
        // We try to find a common prefix and from that on, we infer actions
        List<CollectionAction<Collection<?>>> actions = new ArrayList<>();
        int lastUnmatchedIndex = 0;

        if (initial != null && !initial.isEmpty()) {
            int initialSize = initial.size();
            if (elementDescriptor.isSubview() && elementDescriptor.isIdentifiable()) {
                final AttributeAccessor subviewIdAccessor = elementDescriptor.getViewToEntityMapper().getViewIdAccessor();

                for (int i = 0; i < initialSize; i++) {
                    Object initialViewId = subviewIdAccessor.getValue(initial.get(i));
                    if (i < current.size()) {
                        Object currentViewId = subviewIdAccessor.getValue(current.get(i));
                        if (!initialViewId.equals(currentViewId)) {
                            break;
                        } else {
                            lastUnmatchedIndex++;
                        }
                    } else {
                        // remove all following elements and Keep the same index 'i'
                        if (i < initialSize) {
                            for (int j = i; j < initialSize - 1; j++) {
                                actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) new ListRemoveAction<>(i, false, initial));
                            }
                            actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) new ListRemoveAction<>(i, true, initial));
                        }

                        // Break since there are no more elements to check
                        lastUnmatchedIndex = initialSize;
                        break;
                    }
                }
            } else {
                for (int i = 0; i < initialSize; i++) {
                    Object initialElement = initial.get(i);
                    if (i < current.size()) {
                        Object viewElement = current.get(i);
                        if (!equalityChecker.isEqual(context, initialElement, viewElement)) {
                            break;
                        } else {
                            lastUnmatchedIndex++;
                        }
                    } else {
                        // remove all following elements and Keep the same index 'i'
                        if (i < initialSize) {
                            for (int j = i; j < initialSize - 1; j++) {
                                actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) new ListRemoveAction<>(i, false, initial));
                            }
                            actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) new ListRemoveAction<>(i, true, initial));
                        }

                        // Break since there are no more elements to check
                        lastUnmatchedIndex = initialSize;
                        break;
                    }
                }
            }

            // Remove remaining elements in the list that couldn't be matched
            if (lastUnmatchedIndex < initialSize) {
                for (int i = lastUnmatchedIndex; i < initialSize - 1; i++) {
                    actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) new ListRemoveAction<>(lastUnmatchedIndex, false, initial));
                }
                actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) new ListRemoveAction<>(lastUnmatchedIndex, true, initial));
            }
        }
        // Add new elements that are not matched
        if (lastUnmatchedIndex < current.size()) {
            for (int i = lastUnmatchedIndex; i < current.size(); i++) {
                actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) new ListAddAction<>(lastUnmatchedIndex, true, current.get(i)));
            }
        }

        return actions;
    }
}
