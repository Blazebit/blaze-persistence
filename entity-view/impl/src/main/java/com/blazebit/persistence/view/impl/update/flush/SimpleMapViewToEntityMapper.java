/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.entity.MapViewToEntityMapper;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SimpleMapViewToEntityMapper implements MapViewToEntityMapper {

    private final ViewToEntityMapper keyMapper;
    private final ViewToEntityMapper valueMapper;

    public SimpleMapViewToEntityMapper(ViewToEntityMapper keyMapper, ViewToEntityMapper valueMapper) {
        this.keyMapper = keyMapper;
        this.valueMapper = valueMapper;
    }

    @Override
    public ViewToEntityMapper getKeyMapper() {
        return keyMapper;
    }

    @Override
    public ViewToEntityMapper getValueMapper() {
        return valueMapper;
    }
}
