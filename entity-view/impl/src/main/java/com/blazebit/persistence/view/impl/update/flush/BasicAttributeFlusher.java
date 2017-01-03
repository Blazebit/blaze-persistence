/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.view.impl.update.flush;

import javax.persistence.Query;

import com.blazebit.reflection.PropertyPathExpression;

public class BasicAttributeFlusher<E, V> implements DirtyAttributeFlusher<E, V> {

    private final String parameterName;
    private final PropertyPathExpression<E, V> propertyPath;
    
    public BasicAttributeFlusher(String parameterName, PropertyPathExpression<E, V> propertyPath) {
        this.parameterName = parameterName;
        this.propertyPath = propertyPath;
    }

    @Override
    public boolean supportsQueryFlush() {
        return true;
    }

    @Override
    public void flushQuery(Query query, V value) {
        query.setParameter(parameterName, value);
    }

    @Override
    public void flushEntity(E entity, V value) {
        propertyPath.setValue(entity, value);
    }
}
