/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractMapInstantiator<C extends Map<?, ?>, R extends RecordingMap<C, ?, ?>> implements MapInstantiatorImplementor<C, R> {

    private final PluralObjectFactory<Map<?, ?>> collectionFactory;

    public AbstractMapInstantiator(PluralObjectFactory<Map<?, ?>> collectionFactory) {
        this.collectionFactory = collectionFactory;
    }

    @Override
    public final Map<?, ?> createJpaMap(int size) {
        return collectionFactory.createCollection(size);
    }
}
