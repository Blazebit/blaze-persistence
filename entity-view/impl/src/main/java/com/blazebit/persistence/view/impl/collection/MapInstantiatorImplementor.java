/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import com.blazebit.persistence.view.MapInstantiator;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface MapInstantiatorImplementor<C extends Map<?, ?>, R extends RecordingMap<C, ?, ?>> extends MapInstantiator<C, R> {

    /**
     * Creates a map for the JPA model.
     *
     * @param size The size estimate
     * @return the map
     */
    public Map<?, ?> createJpaMap(int size);

}
