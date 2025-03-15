/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer;

import java.util.Map;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.collection.CollectionInstantiatorImplementor;
import com.blazebit.persistence.view.spi.type.TypeConverter;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class NonIndexedTupleListTransformerFactory implements TupleListTransformerFactory {

    protected final String attributePath;
    private final int startIndex;
    private final int[] parentIdPositions;
    private final CollectionInstantiatorImplementor<?, ?> collectionInstantiator;
    private final boolean dirtyTracking;
    private final TypeConverter<Object, Object> elementConverter;

    public NonIndexedTupleListTransformerFactory(String attributePath, int[] parentIdPositions, int startIndex, CollectionInstantiatorImplementor<?, ?> collectionInstantiator, boolean dirtyTracking, TypeConverter<Object, Object> elementConverter) {
        this.attributePath = attributePath;
        this.startIndex = startIndex;
        this.parentIdPositions = parentIdPositions;
        this.collectionInstantiator = collectionInstantiator;
        this.dirtyTracking = dirtyTracking;
        this.elementConverter = elementConverter;
    }

    @Override
    public int getConsumableIndex() {
        return -1;
    }

    @Override
    public TupleListTransformer create(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EntityViewConfiguration config) {
        if (!config.hasSubFetches(attributePath)) {
            return new NullListTupleTransformer(startIndex, startIndex);
        }
        return new NonIndexedTupleListTransformer(parentIdPositions, startIndex, collectionInstantiator, dirtyTracking, elementConverter);
    }

}
