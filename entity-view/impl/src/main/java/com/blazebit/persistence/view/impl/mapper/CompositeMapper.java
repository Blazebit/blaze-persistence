/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.mapper;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class CompositeMapper<S, T> implements Mapper<S, T> {

    private final Mapper<S, T>[] mappers;

    public CompositeMapper(Mapper<S, T>[] mappers) {
        this.mappers = mappers;
    }

    @Override
    public void map(S source, T target) {
        for (int i = 0; i < mappers.length; i++) {
            mappers[i].map(source, target);
        }
    }
    
}
