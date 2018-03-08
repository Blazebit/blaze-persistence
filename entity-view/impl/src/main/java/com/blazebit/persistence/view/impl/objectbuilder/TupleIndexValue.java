/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
