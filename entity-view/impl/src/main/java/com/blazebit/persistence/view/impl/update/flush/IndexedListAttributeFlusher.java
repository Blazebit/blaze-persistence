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
import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.accessor.InitialValueAttributeAccessor;
import com.blazebit.persistence.view.impl.collection.CollectionAction;
import com.blazebit.persistence.view.impl.collection.CollectionAddAllAction;
import com.blazebit.persistence.view.impl.collection.CollectionInstantiator;
import com.blazebit.persistence.view.impl.collection.ListRemoveAction;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.collection.RecordingList;
import com.blazebit.persistence.view.impl.collection.CollectionRemoveListener;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.proxy.DirtyStateTrackable;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.spi.type.BasicUserType;

import javax.persistence.EntityManager;
import java.util.ArrayList;
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
    public IndexedListAttributeFlusher(String attributeName, String mapping, Class<?> ownerEntityClass, String ownerIdAttributeName, FlushStrategy flushStrategy, AttributeAccessor attributeMapper, InitialValueAttributeAccessor viewAttributeAccessor, boolean optimisticLockProtected, boolean collectionUpdatable,
                                       boolean viewOnlyDeleteCascaded, boolean jpaProviderDeletesCollection, CollectionRemoveListener cascadeDeleteListener, CollectionRemoveListener removeListener, CollectionInstantiator collectionInstantiator, TypeDescriptor elementDescriptor, InverseFlusher<E> inverseFlusher, InverseRemoveStrategy inverseRemoveStrategy) {
        super(attributeName, mapping, ownerEntityClass, ownerIdAttributeName, flushStrategy, attributeMapper, viewAttributeAccessor, optimisticLockProtected, collectionUpdatable, viewOnlyDeleteCascaded, jpaProviderDeletesCollection, cascadeDeleteListener, removeListener, collectionInstantiator, elementDescriptor, inverseFlusher, inverseRemoveStrategy);
    }

    public IndexedListAttributeFlusher(IndexedListAttributeFlusher<E, V> original, boolean fetch) {
        super(original, fetch);
    }

    public IndexedListAttributeFlusher(IndexedListAttributeFlusher<E, V> original, boolean fetch, PluralFlushOperation flushOperation, List<? extends CollectionAction<?>> collectionActions, List<CollectionElementAttributeFlusher<E, V>> elementFlushers) {
        super(original, fetch, flushOperation, collectionActions, elementFlushers);
    }

    @Override
    protected boolean collectionEquals(V initial, V current) {
        if (initial.size() != current.size()) {
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
                for (int j = i; j < jpaSize; j++) {
                    actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) new ListRemoveAction<>(i, jpaCollection));
                }

                // Break since there are no more elements to check
                lastUnmatchedIndex = jpaSize;
                break;
            }
        }
        // Remove remaining elements in the list that couldn't be matched
        for (int i = lastUnmatchedIndex; i < jpaSize; i++) {
            actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) new ListRemoveAction<>(lastUnmatchedIndex, jpaCollection));
        }
        // Add new elements that are not matched
        if (lastUnmatchedIndex < value.size()) {
            actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) new CollectionAddAllAction<>(value.subList(lastUnmatchedIndex, value.size()), true));
        }

        return actions;
    }

    @Override
    protected List<CollectionAction<Collection<?>>> determineCollectionActions(UpdateContext context, V initial, V current, EqualityChecker equalityChecker) {
        // We try to find a common prefix and from that on, we infer actions
        List<CollectionAction<Collection<?>>> actions = new ArrayList<>();
        int initialSize = initial.size();
        int lastUnmatchedIndex = 0;

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
                    for (int j = i; j < initialSize; j++) {
                        actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) new ListRemoveAction<>(i, initial));
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
                    for (int j = i; j < initialSize; j++) {
                        actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) new ListRemoveAction<>(i, initial));
                    }

                    // Break since there are no more elements to check
                    lastUnmatchedIndex = initialSize;
                    break;
                }
            }
        }

        // Remove remaining elements in the list that couldn't be matched
        for (int i = lastUnmatchedIndex; i < initialSize; i++) {
            actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) new ListRemoveAction<>(lastUnmatchedIndex, initial));
        }
        // Add new elements that are not matched
        if (lastUnmatchedIndex < current.size()) {
            actions.add((CollectionAction<Collection<?>>) (CollectionAction<?>) new CollectionAddAllAction<>(current.subList(lastUnmatchedIndex, current.size()), true));
        }

        return actions;
    }
}
