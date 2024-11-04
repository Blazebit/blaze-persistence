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
public class NullMapper<S, T> implements Mapper<S, T> {

    private final Mapper<S, T> mapper;

    public NullMapper(Mapper<S, T> mapper) {
        this.mapper = mapper;
    }

    @Override
    public void map(S source, T target) {
        mapper.map(null, target);
    }
    
}
