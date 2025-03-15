/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer;

import java.util.Map;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.objectbuilder.ContainerAccumulator;
import com.blazebit.persistence.view.spi.type.TypeConverter;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class IndexedTupleListTransformerFactory implements TupleListTransformerFactory {

    protected final String attributePath;
    private final int startIndex;
    private final int[] parentIdPositions;
    private final int valueStartIndex;
    private final ContainerAccumulator<Object> containerAccumulator;
    private final boolean dirtyTracking;
    private final TypeConverter<Object, Object> keyConverter;
    private final TypeConverter<Object, Object> valueConverter;

    public IndexedTupleListTransformerFactory(String attributePath, int[] parentIdPositions, int startIndex, int valueStartIndex, ContainerAccumulator<?> containerAccumulator, boolean dirtyTracking, TypeConverter<Object, Object> keyConverter, TypeConverter<Object, Object> valueConverter) {
        this.attributePath = attributePath;
        this.startIndex = startIndex;
        this.parentIdPositions = parentIdPositions;
        this.valueStartIndex = valueStartIndex;
        this.containerAccumulator = (ContainerAccumulator<Object>) containerAccumulator;
        this.dirtyTracking = dirtyTracking;
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;
    }

    @Override
    public int getConsumableIndex() {
        return startIndex;
    }

    @Override
    public TupleListTransformer create(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EntityViewConfiguration config) {
        if (!config.hasSubFetches(attributePath)) {
            return new NullListTupleTransformer(startIndex, valueStartIndex);
        }
        return new IndexedTupleListTransformer(parentIdPositions, startIndex, valueStartIndex, containerAccumulator, dirtyTracking, keyConverter, valueConverter);
    }

}
