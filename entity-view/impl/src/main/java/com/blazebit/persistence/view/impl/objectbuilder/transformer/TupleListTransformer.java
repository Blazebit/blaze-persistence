/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class TupleListTransformer {

    protected final int startIndex;

    public TupleListTransformer(int startIndex) {
        this.startIndex = startIndex;
    }

    public abstract int getConsumableIndex();

    public abstract List<Object[]> transform(List<Object[]> tuples);
}
