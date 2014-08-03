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
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.view.SubqueryProvider;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Christian
 */
public class ViewTypeObjectBuilder<T> implements ObjectBuilder<T>{

    protected final Constructor<? extends T> proxyConstructor;
    protected final Object[][] mappings;
    
    public ViewTypeObjectBuilder(ViewTypeObjectBuilderTemplate<T> template) {
        this.proxyConstructor = template.getProxyConstructor();
        this.mappings = template.getMappings();
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
    public void applySelects(QueryBuilder<?, ?> queryBuilder) {
        for (Object[] mapping : mappings) {
            if (mapping[0] instanceof Class) {
                Class<? extends SubqueryProvider> subqueryProviderClass = (Class<? extends SubqueryProvider>) mapping[0];
                SubqueryProvider provider;
                
                try {
                    provider = subqueryProviderClass.newInstance();
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Could not instantiate the subquery provider: " + subqueryProviderClass.getName(), ex);
                }

                if (mapping[1] != null) {
                    provider.createSubquery(queryBuilder.selectSubquery((String) mapping[1]));
                } else {
                    provider.createSubquery(queryBuilder.selectSubquery());
                }
            } else {
                if (mapping[1] != null) {
                    queryBuilder.select((String) mapping[0], (String) mapping[1]);
                } else {
                    queryBuilder.select((String) mapping[0]);
                }
            }
        }
    }
}
