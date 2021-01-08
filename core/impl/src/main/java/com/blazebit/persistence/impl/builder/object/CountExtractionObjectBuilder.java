/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.impl.builder.object;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectBuilder;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class CountExtractionObjectBuilder<T> implements ObjectBuilder<T> {

    private final ObjectBuilder<T> delegate;
    private long count = -1;

    public CountExtractionObjectBuilder(ObjectBuilder<T> delegate) {
        this.delegate = delegate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T build(Object[] tuple) {
        count = (long) tuple[tuple.length - 1];

        Object[] newTuple = new Object[tuple.length - 1];
        System.arraycopy(tuple, 0, newTuple, 0, newTuple.length);
        return delegate.build(newTuple);
    }

    public long getCount() {
        return count;
    }

    @Override
    public List<T> buildList(List<T> list) {
        return delegate.buildList(list);
    }

    @Override
    public <X extends SelectBuilder<X>> void applySelects(X queryBuilder) {
        delegate.applySelects(queryBuilder);
    }

}
