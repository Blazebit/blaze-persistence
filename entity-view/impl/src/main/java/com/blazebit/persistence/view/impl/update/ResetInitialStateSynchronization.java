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

import com.blazebit.persistence.view.impl.collection.CollectionAction;
import com.blazebit.persistence.view.impl.collection.MapAction;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.impl.proxy.DirtyTracker;
import com.blazebit.persistence.view.impl.proxy.MutableStateTrackable;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ResetInitialStateSynchronization implements Synchronization, InitialStateResetter {

    private static final Object NO_ID_MARKER = new Object();
    private List<Object[]> coalescedInitialStates;
    private List<Object> coalescedRecordingActions;
    private List<Object> persistedViews;
    private List<Object> updatedViews;
    private List<Object> removedViews;
    private List<Object> versionedViews;

    @Override
    public void addRecordingCollection(RecordingCollection<?, ?> recordingCollection, List<? extends CollectionAction<?>> actions, Map<?, ?> addedElements, Map<?, ?> removedElements) {
        if (coalescedRecordingActions == null) {
            coalescedRecordingActions = new ArrayList<>();
        }
        coalescedRecordingActions.add(recordingCollection);
        coalescedRecordingActions.add(actions);
        coalescedRecordingActions.add(addedElements);
        coalescedRecordingActions.add(removedElements);
    }

    @Override
    public void addRecordingMap(RecordingMap<?, ?, ?> recordingMap, List<? extends MapAction<?>> actions, Map<?, ?> addedKeys, Map<?, ?> removedKeys, Map<?, ?> addedElements, Map<?, ?> removedElements) {
        if (coalescedRecordingActions == null) {
            coalescedRecordingActions = new ArrayList<>();
        }
        coalescedRecordingActions.add(recordingMap);
        coalescedRecordingActions.add(actions);
        coalescedRecordingActions.add(addedKeys);
        coalescedRecordingActions.add(removedKeys);
        coalescedRecordingActions.add(addedElements);
        coalescedRecordingActions.add(removedElements);
    }

    @Override
    public int addPersistedView(MutableStateTrackable persistedView) {
        if (persistedViews == null) {
            persistedViews = new ArrayList<>();
        }
        persistedView.$$_setIsNew(false);
        persistedViews.add(persistedView);
        persistedViews.add(null);
        persistedViews.add(NO_ID_MARKER);
        persistedViews.add(persistedView.$$_getParent());
        persistedViews.add(persistedView.$$_getParentIndex());
        persistedViews.add(persistedView.$$_getReadOnlyParents() == null || persistedView.$$_getReadOnlyParents().isEmpty() ? Collections.emptyList() : new ArrayList<>(persistedView.$$_getReadOnlyParents()));
        persistedViews.add(persistedView.$$_resetDirty());
        return persistedViews.size() - 6;
    }

    @Override
    public int addPersistedView(MutableStateTrackable persistedView, Object oldId) {
        if (persistedViews == null) {
            persistedViews = new ArrayList<>();
        }
        persistedView.$$_setIsNew(false);
        persistedViews.add(persistedView);
        persistedViews.add(null);
        persistedViews.add(oldId);
        persistedViews.add(persistedView.$$_getParent());
        persistedViews.add(persistedView.$$_getParentIndex());
        persistedViews.add(persistedView.$$_getReadOnlyParents() == null || persistedView.$$_getReadOnlyParents().isEmpty() ? Collections.emptyList() : new ArrayList<>(persistedView.$$_getReadOnlyParents()));
        persistedViews.add(persistedView.$$_resetDirty());
        return persistedViews.size() - 6;
    }

    @Override
    public void addPersistedViewNewObject(int newObjectIndex, Object newObject) {
        persistedViews.set(newObjectIndex, newObject);
    }

    @Override
    public void addUpdatedView(MutableStateTrackable updatedView) {
        if (updatedViews == null) {
            updatedViews = new ArrayList<>();
        }
        updatedViews.add(updatedView);
        updatedViews.add(updatedView.$$_resetDirty());
    }

    @Override
    public void addRemovedView(EntityViewProxy view) {
        if (removedViews == null) {
            removedViews = new ArrayList<>();
        }
        removedViews.add(view);
        if (view instanceof MutableStateTrackable) {
            MutableStateTrackable removedView = (MutableStateTrackable) view;
            removedViews.add(removedView.$$_getParent());
            removedViews.add(removedView.$$_getParentIndex());
            removedViews.add(removedView.$$_resetDirty());
            removedView.$$_unsetParent();
        } else {
            removedViews.add(null);
            removedViews.add(null);
            removedViews.add(null);
        }
    }

    @Override
    public void addVersionedView(MutableStateTrackable updatedView, Object oldVersion) {
        if (versionedViews == null) {
            versionedViews = new ArrayList<>();
        }
        versionedViews.add(updatedView);
        versionedViews.add(oldVersion);
    }

    @Override
    public void addState(Object[] reference, Object[] copy) {
        if (coalescedInitialStates == null) {
            coalescedInitialStates = new ArrayList<>();
        }
        coalescedInitialStates.add(reference);
        coalescedInitialStates.add(copy);
    }

    @Override
    public void beforeCompletion() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterCompletion(int status) {
        if (status != Status.STATUS_COMMITTED) {
            if (coalescedInitialStates != null) {
                for (int i = 0; i < coalescedInitialStates.size(); i += 2) {
                    Object[] initialState = coalescedInitialStates.get(i);
                    Object[] originalInitialState = coalescedInitialStates.get(i + 1);
                    for (int j = 0; j < initialState.length; j++) {
                        initialState[j] = originalInitialState[j];
                    }
                }
            }
            if (coalescedRecordingActions != null) {
                for (int i = 0; i < coalescedRecordingActions.size(); i += 4) {
                    Object collectionReference = coalescedRecordingActions.get(i);
                    Object actionList = coalescedRecordingActions.get(i + 1);
                    Map<Object, Object> added = (Map<Object, Object>) coalescedRecordingActions.get(i + 2);
                    Map<Object, Object> removed = (Map<Object, Object>) coalescedRecordingActions.get(i + 3);

                    if (collectionReference instanceof RecordingCollection<?, ?>) {
                        RecordingCollection<Collection<Object>, Object> collection = (RecordingCollection<Collection<Object>, Object>) collectionReference;
                        collection.setActions((List<CollectionAction<Collection<Object>>>) (List<?>) actionList, added, removed);
                        collection.$$_markDirty(-1);
                    } else {
                        Map<Object, Object> addedElements = (Map<Object, Object>) coalescedRecordingActions.get(i + 4);
                        Map<Object, Object> removedElements = (Map<Object, Object>) coalescedRecordingActions.get(i + 5);
                        RecordingMap<Map<Object, Object>, Object, Object> collection = (RecordingMap<Map<Object, Object>, Object, Object>) collectionReference;
                        collection.setActions((List<MapAction<Map<Object, Object>>>) (List<?>) actionList, added, removed, addedElements, removedElements);
                        collection.$$_markDirty(-1);
                        i += 2;
                    }
                }
            }
            if (persistedViews != null) {
                for (int i = 0; i < persistedViews.size(); i += 7) {
                    MutableStateTrackable view = (MutableStateTrackable) persistedViews.get(i);
                    Object newObject = persistedViews.get(i + 1);
                    view.$$_setIsNew(true);
                    Object id = persistedViews.get(i + 2);
                    DirtyTracker parent = (DirtyTracker) persistedViews.get(i + 3);
                    int parentIndex = (int) persistedViews.get(i + 4);
                    List<Object> readOnlyParents = (List<Object>) persistedViews.get(i + 5);
                    if (id != NO_ID_MARKER) {
                        view.$$_setId(id);
                    }
                    if (parent != null) {
                        // This is not needed as this will happen when rolling back collection actions already
                        if (!(parent instanceof RecordingCollection<?, ?> || parent instanceof RecordingMap<?, ?, ?>)) {
                            parent.$$_replaceAttribute(newObject, parentIndex, view);
                        }
                        for (int j = 0; j < readOnlyParents.size(); j += 2) {
                            DirtyTracker readOnlyParent = (DirtyTracker) readOnlyParents.get(j);
                            int readOnlyParentIndex = (int) readOnlyParents.get(j + 1);
                            readOnlyParent.$$_replaceAttribute(newObject, readOnlyParentIndex, view);
                        }
                    }
                    view.$$_setDirty((long[]) persistedViews.get(i + 6));
                }
            }
            if (updatedViews != null) {
                for (int i = 0; i < updatedViews.size(); i += 2) {
                    MutableStateTrackable view = (MutableStateTrackable) updatedViews.get(i);
                    view.$$_setDirty((long[]) updatedViews.get(i + 1));
                }
            }
            if (removedViews != null) {
                for (int i = 0; i < removedViews.size(); i += 4) {
                    EntityViewProxy view = (EntityViewProxy) removedViews.get(i);
                    if (view instanceof MutableStateTrackable) {
                        MutableStateTrackable removedView = (MutableStateTrackable) view;
                        DirtyTracker parent = (DirtyTracker) removedViews.get(i + 1);
                        if (parent != null) {
                            removedView.$$_setParent(parent, (Integer) removedViews.get(i + 2));
                        }
                        long[] dirtyArray = (long[]) removedViews.get(i + 3);
                        if (dirtyArray != null) {
                            removedView.$$_setDirty(dirtyArray);
                        }
                    }
                }
            }
            if (versionedViews != null) {
                for (int i = 0; i < versionedViews.size(); i += 2) {
                    MutableStateTrackable view = (MutableStateTrackable) versionedViews.get(i);
                    view.$$_setVersion(versionedViews.get(i + 1));
                }
            }
        }
    }

}
