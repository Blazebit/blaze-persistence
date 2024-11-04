/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapFactory implements PluralObjectFactory<Map<?, ?>> {

    public static final MapFactory INSTANCE = new MapFactory();

    @Override
    public Map<?, ?> createCollection(int size) {
        return new HashMap<>();
    }

}
