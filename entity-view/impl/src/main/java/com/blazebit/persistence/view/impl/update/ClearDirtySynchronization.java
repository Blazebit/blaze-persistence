/*
 * Copyright 2014 - 2017 Blazebit.
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

import javax.transaction.Status;
import javax.transaction.Synchronization;

import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.collection.RecordingMap;

public class ClearDirtySynchronization implements Synchronization {
    
    private final Object[] initialState;
    private final Object[] originalDirtyState;
    private final Object[] dirtyState;

    public ClearDirtySynchronization(Object[] initialState, Object[] originalDirtyState, Object[] dirtyState) {
        this.initialState = initialState;
        this.originalDirtyState = originalDirtyState;
        this.dirtyState = dirtyState;
    }

    @Override
    public void beforeCompletion() {
    }

    @Override
    public void afterCompletion(int status) {
        if (status == Status.STATUS_COMMITTED) {
            for (int i = 0; i < dirtyState.length; i++) {
                if (originalDirtyState[i] == dirtyState[i]) {
                    initialState[i] = dirtyState[i];
                    
                    // TODO: what happens when new actions happen?
                    if (dirtyState[i] instanceof RecordingCollection<?, ?>) {
                        ((RecordingCollection<?, ?>) dirtyState[i]).clearActions();
                    } else if (dirtyState[i] instanceof RecordingMap<?, ?, ?>) {
                        ((RecordingMap<?, ?, ?>) dirtyState[i]).clearActions();
                    }
                }
            }
        }
    }

}
