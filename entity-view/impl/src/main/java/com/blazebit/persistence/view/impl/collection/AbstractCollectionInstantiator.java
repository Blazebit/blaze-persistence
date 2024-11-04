/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import java.util.Collection;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCollectionInstantiator<C extends Collection<?>, R extends RecordingCollection<? extends C, ?>> implements CollectionInstantiatorImplementor<C, R> {

    private final PluralObjectFactory<Collection<?>> collectionFactory;

    public AbstractCollectionInstantiator(PluralObjectFactory<Collection<?>> collectionFactory) {
        this.collectionFactory = collectionFactory;
    }

    @Override
    public boolean requiresPostConstruct() {
        return false;
    }

    @Override
    public void postConstruct(Collection<?> collection) {
    }

    @Override
    public final Collection<?> createJpaCollection(int size) {
        return collectionFactory.createCollection(size);
    }
}
