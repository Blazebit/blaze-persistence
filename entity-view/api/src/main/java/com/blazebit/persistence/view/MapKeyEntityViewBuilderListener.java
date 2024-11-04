/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import java.util.Map;

/**
 * A listener that adds the built entity view to a map.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class MapKeyEntityViewBuilderListener implements EntityViewBuilderListener {

    private final Map<Object, Object> map;
    private Object key;

    /**
     * Creates the listener.
     *
     * @param map The map to add a built entity view to
     */
    public MapKeyEntityViewBuilderListener(Map<Object, Object> map) {
        this.map = map;
    }

    @Override
    public void onBuildComplete(Object object) {
        if (key == null) {
            key = object;
        } else {
            map.put(key, object);
        }
    }
}
