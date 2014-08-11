/*
 * Copyright 2014 Blazebit.
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

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.TupleElementMapper;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class ViewTypeObjectBuilder<T> implements ObjectBuilder<T> {

    protected final Constructor<? extends T> proxyConstructor;
    protected final TupleElementMapper[] mappers;

    public ViewTypeObjectBuilder(ViewTypeObjectBuilderTemplate<T> template) {
        this.proxyConstructor = template.getProxyConstructor();
        this.mappers = template.getMappers();
    }

    @Override
    public T build(Object[] tuple) {
        if (tuple[0] == null) {
            return null;
        }

        try {
            return proxyConstructor.newInstance(tuple);
        } catch (Exception ex) {
            throw new RuntimeException("Could not invoke the proxy constructor '" + proxyConstructor + "' with the given tuple: " + Arrays.toString(tuple), ex);
        }
    }

    @Override
    public List<T> buildList(List<T> list) {
        return list;
    }

    @Override
    public void applySelects(SelectBuilder<?, ?> queryBuilder) {
        for (int i = 0; i < mappers.length; i++) {
            mappers[i].applyMapping(queryBuilder);
        }
    }
}
