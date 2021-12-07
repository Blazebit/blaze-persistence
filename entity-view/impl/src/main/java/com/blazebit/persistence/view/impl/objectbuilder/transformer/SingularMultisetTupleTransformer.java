/*
 * Copyright 2014 - 2021 Blazebit.
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

import com.blazebit.persistence.view.impl.objectbuilder.transformator.TupleTransformator;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.UpdatableViewMap;
import com.blazebit.persistence.view.spi.type.BasicUserTypeStringSupport;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class SingularMultisetTupleTransformer<C> implements TupleTransformer {

    private final int startIndex;
    private final boolean hasSelectOrSubselectFetchedAttributes;
    private final TupleTransformator tupleTransformator;
    private final TupleTransformer subviewTupleTransformer;
    private final BasicUserTypeStringSupport<Object>[] fieldConverters;
    private final TypeConverter<Object, Object> elementConverter;

    public SingularMultisetTupleTransformer(int startIndex, boolean hasSelectOrSubselectFetchedAttributes, TupleTransformator tupleTransformator, TupleTransformer subviewTupleTransformer, BasicUserTypeStringSupport<Object>[] fieldConverters, TypeConverter<Object, Object> elementConverter) {
        this.startIndex = startIndex;
        this.hasSelectOrSubselectFetchedAttributes = hasSelectOrSubselectFetchedAttributes;
        this.tupleTransformator = tupleTransformator;
        this.subviewTupleTransformer = subviewTupleTransformer;
        this.elementConverter = elementConverter;
        this.fieldConverters = fieldConverters;
    }

    @Override
    public int getConsumeStartIndex() {
        return startIndex;
    }

    @Override
    public int getConsumeEndIndex() {
        return startIndex + 1;
    }

    @Override
    public Object[] transform(Object[] tuple, UpdatableViewMap updatableViewMap) {
        // First, go through all multi-sets and convert the fields
        if (tuple[startIndex] != null) {
            List<Object[]> objects = (List<Object[]>) tuple[startIndex];
            for (int i = 0; i < objects.size(); i++) {
                Object[] elementTuple = objects.get(i);
                for (int j = 0; j < fieldConverters.length; j++) {
                    if (elementTuple[j] instanceof CharSequence) {
                        elementTuple[j] = fieldConverters[j].fromString((CharSequence) elementTuple[j]);
                    }
                }
            }
            // Before building the subviews, apply the tuple transformator on the nested set in isolation
            tupleTransformator.transformAll(objects);
            if (!objects.isEmpty()) {
                // Build views and add them to collections
                Object[] transformedTuple = subviewTupleTransformer.transform(objects.get(0), updatableViewMap);
                if (elementConverter == null) {
                    tuple[startIndex] = transformedTuple[0];
                } else {
                    tuple[startIndex] = elementConverter.convertToViewType(transformedTuple[0]);
                }
            }
        }
        return tuple;
    }

}
