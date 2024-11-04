/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder;

import com.blazebit.persistence.view.impl.proxy.ObjectInstantiator;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class InheritanceReducerViewTypeObjectBuilder<T> extends ReducerViewTypeObjectBuilder<T> {

    private final boolean hasId;
    private final boolean nullIfEmpty;
    private final int subtypeDiscriminatorIndex;
    private final ObjectInstantiator<T>[] subtypeInstantiators;

    public InheritanceReducerViewTypeObjectBuilder(ViewTypeObjectBuilder<T> delegate, int subtypeDiscriminatorIndex, int suffix, int length, boolean keepTuplePrefix, ObjectInstantiator<T>[] subtypeInstantiators) {
        super(delegate, subtypeDiscriminatorIndex + 1, suffix, length - 1, keepTuplePrefix);
        this.hasId = delegate.hasId;
        this.nullIfEmpty = delegate.nullIfEmpty;
        this.subtypeDiscriminatorIndex = subtypeDiscriminatorIndex;
        this.subtypeInstantiators = subtypeInstantiators;
    }

    @Override
    protected T buildObject(Object[] originalTuple, Object[] tuple) {
        // Cast to Number instead of integer since datanucleus will return a Long
        Number index = (Number) originalTuple[subtypeDiscriminatorIndex];
        if (index == null) {
            return null;
        } else {
            if (hasId) {
                if (tuple[0] == null) {
                    return null;
                }
            } else if (nullIfEmpty) {
                for (int i = 0; i < tuple.length; i++) {
                    if (tuple[i] != null) {
                        return subtypeInstantiators[index.intValue()].newInstance(tuple);
                    }
                }

                return null;
            }
            return subtypeInstantiators[index.intValue()].newInstance(tuple);
        }
    }
}
