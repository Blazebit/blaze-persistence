/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.mapper;

import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class TargetViewClassBasedMapper<S, T> implements Mapper<S, T> {

    private final Map<Class<?>, Mapper<S, T>> mappers;

    public TargetViewClassBasedMapper(Map<Class<?>, Mapper<S, T>> mappers) {
        this.mappers = mappers;
    }

    @Override
    public void map(S source, T target) {
        Mapper<S, T> mapper = mappers.get(((EntityViewProxy) target).$$_getEntityViewClass());
        mapper.map(source, target);
    }
    
}
