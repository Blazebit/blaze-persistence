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

import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.util.Comparator;
import java.util.TreeSet;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class SortedSetTupleListTransformer extends SetTupleListTransformer {
    
    private final Comparator<Object> comparator;

    @SuppressWarnings("unchecked")
    public SortedSetTupleListTransformer(int[] parentIdPositions, int startIndex, Comparator<?> comparator, TypeConverter<Object, Object> elementConverter) {
        super(parentIdPositions, startIndex, elementConverter);
        this.comparator = (Comparator<Object>) comparator;
    }
    
    @Override
    protected Object createCollection() {
        return new TreeSet<Object>(comparator);
    }

}
