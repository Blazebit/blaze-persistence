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

package com.blazebit.persistence.view.impl.update;

import com.blazebit.persistence.view.impl.collection.CollectionAction;
import com.blazebit.persistence.view.impl.collection.MapAction;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.impl.proxy.MutableStateTrackable;
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

    public void addPersistedView(MutableStateTrackable persistedView);

    public void addPersistedView(MutableStateTrackable persistedView, Object oldId);

    public void addUpdatedView(MutableStateTrackable updatedView);

    public void addRemovedView(EntityViewProxy view);

    public void addVersionedView(MutableStateTrackable updatedView, Object oldVersion);

    public void addState(Object[] reference, Object[] copy);
}
