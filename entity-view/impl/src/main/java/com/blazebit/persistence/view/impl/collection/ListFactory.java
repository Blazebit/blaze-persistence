/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ListFactory implements PluralObjectFactory<List<?>> {

    public static final ListFactory INSTANCE = new ListFactory();

    @Override
    public List<?> createCollection(int size) {
        return new ArrayList<>(size);
    }

}
