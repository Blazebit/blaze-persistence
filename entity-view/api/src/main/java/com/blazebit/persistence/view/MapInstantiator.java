/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import java.util.Map;

/**
 * An instantiator for normal, recording and JPA maps for an entity view attribute.
 *
 * @param <C> The map type
 * @param <R> The recording container type
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface MapInstantiator<C extends Map<?, ?>, R extends Map<?, ?> & RecordingContainer<? extends C>> {

    /**
     * Creates a plain map.
     *
     * @param size The size estimate
     * @return the map
     */
    public C createMap(int size);

    /**
     * Creates a recording map.
     *
     * @param size The size estimate
     * @return the recording map
     */
    public R createRecordingMap(int size);
}