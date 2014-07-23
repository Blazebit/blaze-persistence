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

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.QueryBuilder;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author cpbec
 */
public class ViewTypeObjectBuilderImpl<T> implements ObjectBuilder<T> {
    
    public final Constructor<? extends T> proxyConstructor;
    public final String[] mappings;
    public final String[] parameterMappings;
    public final boolean hasParameters;
    private final QueryBuilder<?, ?> queryBuilder;

    public ViewTypeObjectBuilderImpl(ViewTypeObjectBuilderTemplate<T> template, QueryBuilder<?, ?> queryBuilder) {
        this.proxyConstructor = template.getProxyConstructor();
        this.mappings = template.getMappings();
        this.parameterMappings = template.getParameterMappings();
        this.hasParameters = template.hasParameters();
        this.queryBuilder = queryBuilder;
    }

    @Override
    public T build(Object[] tuple, String[] aliases) {
        if (hasParameters) {
            try {
                for (int i = 0; i < tuple.length; i++) {
                    if (parameterMappings[i] != null) {
                        tuple[i] = queryBuilder.getParameterValue(parameterMappings[i]);
                    }
                }
                return proxyConstructor.newInstance(tuple);
            } catch (Exception ex) {
                throw new RuntimeException("Could not invoke the proxy constructor '" + proxyConstructor + "' with the given tuple: " + Arrays.toString(tuple), ex);
            }
        } else {
            try {
                return proxyConstructor.newInstance(tuple);
            } catch (Exception ex) {
                throw new RuntimeException("Could not invoke the proxy constructor '" + proxyConstructor + "' with the given tuple: " + Arrays.toString(tuple), ex);
            }
        }
    }

    @Override
    public List<T> buildList(List<T> list) {
        return list;
    }
    
    @Override
    public String[] getExpressions() {
        return mappings;
    }
}
