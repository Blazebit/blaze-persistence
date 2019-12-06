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

import com.blazebit.persistence.view.impl.collection.CollectionAction;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class RecordingCollectionResetter implements Runnable {

    private final UpdateContext updateContext;
    private final RecordingCollection<?, ?> recordingCollection;

    public RecordingCollectionResetter(UpdateContext updateContext, RecordingCollection<?, ?> recordingCollection) {
        this.updateContext = updateContext;
        this.recordingCollection = recordingCollection;
    }

    public static List<? extends CollectionAction<?>> add(UpdateContext updateContext, List<Runnable> preFlushListeners, Object o) {
        if (o instanceof RecordingCollection<?, ?>) {
            if (preFlushListeners == null) {
                return ((RecordingCollection<?, ?>) o).resetActions(updateContext);
            }
            preFlushListeners.add(new RecordingCollectionResetter(updateContext, (RecordingCollection<?, ?>) o));
            List<? extends CollectionAction<?>> actions = ((RecordingCollection<?, ?>) o).getActions();
            if (actions != null) {
                return actions;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void run() {
        recordingCollection.resetActions(updateContext);
    }
}
