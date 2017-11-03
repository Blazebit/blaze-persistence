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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.spi.type.TypeConverter;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class UpdatableOrderedMapTupleListTransformer extends UpdatableMapTupleListTransformer {

    public UpdatableOrderedMapTupleListTransformer(int[] parentIdPositions, int startIndex, int valueStartIndex, Set<Class<?>> allowedSubtypes, boolean updatable, TypeConverter<Object, Object> keyConverter, TypeConverter<Object, Object> valueConverter) {
        super(parentIdPositions, startIndex, valueStartIndex, allowedSubtypes, updatable, keyConverter, valueConverter);
    }

    @Override
    protected Object createCollection() {
        return new RecordingMap<Map<Object, Object>, Object, Object>(new LinkedHashMap<Object, Object>(), allowedSubtypes, updatable);
    }

}
