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

package com.blazebit.persistence.view.impl.objectbuilder.transformer;

import com.blazebit.persistence.view.impl.collection.MapInstantiator;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class MapTupleListTransformer extends AbstractIndexedTupleListTransformer<Map<Object, Object>, Object> {

    private final MapInstantiator mapInstantiator;
    private final boolean dirtyTracking;

    public MapTupleListTransformer(int[] parentIdPositions, int startIndex, int valueStartIndex, MapInstantiator mapInstantiator, boolean dirtyTracking, TypeConverter<Object, Object> keyConverter, TypeConverter<Object, Object> valueConverter) {
        super(parentIdPositions, startIndex, valueStartIndex, keyConverter, valueConverter);
        this.mapInstantiator = mapInstantiator;
        this.dirtyTracking = dirtyTracking;
    }

    @Override
    protected Object createCollection() {
        if (dirtyTracking) {
            return mapInstantiator.createRecordingCollection(0);
        } else {
            return mapInstantiator.createCollection(0);
        }
    }

    @Override
    protected void addToCollection(Map<Object, Object> map, Object key, Object value) {
        if (dirtyTracking) {
            ((RecordingMap<?, Object, Object>) map).getDelegate().put(key, value);
        } else {
            map.put(key, value);
        }
    }

}
