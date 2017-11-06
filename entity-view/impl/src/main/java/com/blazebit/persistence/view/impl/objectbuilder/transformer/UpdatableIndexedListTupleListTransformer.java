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

package com.blazebit.persistence.view.impl.objectbuilder.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.blazebit.persistence.view.impl.collection.RecordingList;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class UpdatableIndexedListTupleListTransformer extends AbstractIndexedTupleListTransformer<RecordingList<Object>, Integer> {

    private final Set<Class<?>> allowedSubtypes;
    private final boolean updatable;

    public UpdatableIndexedListTupleListTransformer(int[] parentIdPositions, int startIndex, Set<Class<?>> allowedSubtypes, boolean updatable) {
        super(parentIdPositions, startIndex, startIndex + 1);
        this.allowedSubtypes = allowedSubtypes;
        this.updatable = updatable;
    }

    @Override
    protected Object createCollection() {
        return new RecordingList<Object>(new ArrayList<Object>(), allowedSubtypes, updatable);
    }

    @Override
    protected void addToCollection(RecordingList<Object> recordingList, Integer index, Object value) {
        final List<Object> list = recordingList.getDelegate();
        if (index < list.size()) {
            list.set(index, value);
        } else if (index > list.size()) {
            for (int i = index - list.size(); i <= index; i++) {
                list.add(null);
            }
            
            list.add(index, value);
        } else {
            list.add(index, value);
        }
    }

}