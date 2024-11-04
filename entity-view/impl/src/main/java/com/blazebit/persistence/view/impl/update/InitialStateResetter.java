/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update;

import com.blazebit.persistence.view.impl.collection.CollectionAction;
import com.blazebit.persistence.view.impl.collection.MapAction;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.spi.type.MutableStateTrackable;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface InitialStateResetter {

    public void addRecordingCollection(RecordingCollection<?, ?> recordingCollection, List<? extends CollectionAction<?>> actions, Map<?, ?> addedElements, Map<?, ?> removedElements);

    public void addRecordingMap(RecordingMap<?, ?, ?> recordingMap, List<? extends MapAction<?>> actions, Map<?, ?> addedKeys, Map<?, ?> removedKeys, Map<?, ?> addedElements, Map<?, ?> removedElements);

    public int addPersistedView(MutableStateTrackable persistedView);

    public int addPersistedView(MutableStateTrackable persistedView, Object oldId);

    public void addPersistedViewNewObject(int newObjectIndex, Object newObject);

    public long[] addUpdatedView(MutableStateTrackable updatedView);

    public void addRemovedView(EntityViewProxy view);

    public void addVersionedView(MutableStateTrackable updatedView, Object oldVersion);

    public void addState(Object[] reference, Object[] copy);
}
