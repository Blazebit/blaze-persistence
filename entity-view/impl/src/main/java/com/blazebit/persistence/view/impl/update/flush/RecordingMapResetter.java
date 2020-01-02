/*
 * Copyright 2014 - 2020 Blazebit.
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

import com.blazebit.persistence.view.impl.collection.MapAction;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class RecordingMapResetter implements Runnable {

    private final UpdateContext updateContext;
    private final RecordingMap<?, ?, ?> recordingMap;

    public RecordingMapResetter(UpdateContext updateContext, RecordingMap<?, ?, ?> recordingMap) {
        this.updateContext = updateContext;
        this.recordingMap = recordingMap;
    }

    public static List<? extends MapAction<?>> add(UpdateContext updateContext, List<Runnable> preFlushListeners, Object o) {
        if (o instanceof RecordingMap<?, ?, ?>) {
            if (preFlushListeners == null) {
                return ((RecordingMap<?, ?, ?>) o).resetActions(updateContext);
            }
            preFlushListeners.add(new RecordingMapResetter(updateContext, (RecordingMap<?, ?, ?>) o));
            List<? extends MapAction<?>> actions = ((RecordingMap<?, ?, ?>) o).getActions();
            if (actions != null) {
                return actions;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void run() {
        recordingMap.resetActions(updateContext);
    }
}
