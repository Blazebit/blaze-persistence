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

import java.util.List;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectBuilder;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DelegatingObjectBuilder<T> implements ObjectBuilder<T> {

    protected final ObjectBuilder<T> delegate;

    public DelegatingObjectBuilder(ObjectBuilder<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public <X extends SelectBuilder<X>> void applySelects(X queryBuilder) {
        delegate.applySelects(queryBuilder);
    }

    @Override
    public T build(Object[] tuple) {
        return delegate.build(tuple);
    }

    @Override
    public List<T> buildList(List<T> list) {
        return delegate.buildList(list);
    }
}
