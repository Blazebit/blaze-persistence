/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
