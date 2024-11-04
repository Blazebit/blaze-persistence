/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.mapper;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public final class NoopMapper<S, T> implements Mapper<S, T> {

    public static final Mapper INSTANCE = new NoopMapper();

    private NoopMapper() {
    }

    @Override
    public void map(S source, T target) {
        // No-op
    }

}
