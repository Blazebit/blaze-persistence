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
public class MapEntityViewBuilderListener implements EntityViewBuilderListener {

    private final Map<Object, Object> map;
    private final Object key;

    /**
     * Creates a listener.
     *
     * @param map The map to add the built entity view to
     * @param key The key under which to add the entity view
     */
    public MapEntityViewBuilderListener(Map<Object, Object> map, Object key) {
        this.map = map;
        this.key = key;
    }

    @Override
    public void onBuildComplete(Object object) {
        map.put(key, object);
    }
}
