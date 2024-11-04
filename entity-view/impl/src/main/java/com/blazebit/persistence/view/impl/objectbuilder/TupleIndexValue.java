/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class TupleIndexValue {

    private final Object tupleValue;
    private final Set<TupleRest> restTuples = new HashSet<TupleRest>();

    public TupleIndexValue(Object targetValue, Object[] tuple, int restTupleIndex, int offset) {
        this.tupleValue = targetValue;
        restTuples.add(new TupleRest(tuple, restTupleIndex, offset));
    }

    public Object getTupleValue() {
        return tupleValue;
    }

    public boolean addRestTuple(Object[] tuple, int tupleIndex, int offset) {
        return restTuples.add(new TupleRest(tuple, tupleIndex, offset));
    }

    public boolean containsRestTuple(Object[] tuple, int tupleIndex, int offset) {
        return restTuples.contains(new TupleRest(tuple, tupleIndex + offset));
    }

}
