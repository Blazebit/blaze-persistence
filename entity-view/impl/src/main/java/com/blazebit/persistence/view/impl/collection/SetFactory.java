/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SetFactory implements PluralObjectFactory<Set<?>> {

    public static final SetFactory INSTANCE = new SetFactory();

    @Override
    public Set<?> createCollection(int size) {
        return new HashSet<>(size);
    }

}
