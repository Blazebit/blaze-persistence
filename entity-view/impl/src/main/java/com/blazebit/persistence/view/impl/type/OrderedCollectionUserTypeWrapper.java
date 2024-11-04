/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OrderedCollectionUserTypeWrapper<V> extends AbstractCollectionUserTypeWrapper<List<V>, V> {

    public OrderedCollectionUserTypeWrapper(BasicUserType<V> elementUserType) {
        super(elementUserType);
    }

    @Override
    protected List<V> createCollection(int size) {
        return new ArrayList<>(size);
    }
}
