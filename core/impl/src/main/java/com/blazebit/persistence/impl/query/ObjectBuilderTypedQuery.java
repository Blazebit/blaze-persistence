/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.impl.query;

import com.blazebit.persistence.ObjectBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.ParameterExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ObjectBuilderTypedQuery<X> extends TypedQueryWrapper<X> {

    private final ObjectBuilder<X> builder;

    @SuppressWarnings("unchecked")
    public ObjectBuilderTypedQuery(TypedQuery<?> delegate, Map<ParameterExpression<?>, String> criteriaNameMapping, ObjectBuilder<X> builder) {
        super((TypedQuery<X>) delegate, criteriaNameMapping);
        this.builder = builder;
    }

    @Override
    public X getSingleResult() {
        List<X> list = getResultList();
        
        switch (list.size()) {
            case 0:
                throw new NoResultException("No results for query: " + delegate);
            case 1:
                return list.get(0);
            default:
                throw new NonUniqueResultException("Expected a single result for query: " + delegate);
        }
    }

    @Override
    public List<X> getResultList() {
        List<X> list = super.getResultList();
        int size = list.size();
        List<X> newList = new ArrayList<X>(size);

        for (int i = 0; i < size; i++) {
            Object tuple = list.get(i);
            
            if (tuple instanceof Object[]) {
                newList.add(builder.build((Object[]) tuple));
            } else {
                newList.add(builder.build(new Object[] { tuple }));
            }
        }
        
        return builder.buildList(newList);
    }

    public Stream<X> getResultStream() {
        final Stream<X> resultStream = super.getResultStream();
        return resultStream.map(new Function<X, X>() {
            @Override
            public X apply(X tuple) {
                Object[] array;
                if (tuple instanceof Object[]) {
                    array = (Object[]) tuple;
                } else {
                    array = new Object[]{tuple};
                }
                X result = builder.build(array);
                if (result == array) {
                    throw new UnsupportedOperationException("Object builder is not streaming capable: " + builder);
                }
                return result;
            }
        }).onClose(new Runnable() {
            @Override
            public void run() {
                resultStream.close();
            }
        });
    }

}
