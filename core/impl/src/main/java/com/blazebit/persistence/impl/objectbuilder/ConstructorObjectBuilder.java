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

package com.blazebit.persistence.impl.objectbuilder;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectBuilder;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 *
 * @author Moritz Becker
 */
public class ConstructorObjectBuilder<T> implements ObjectBuilder<T> {
    
    private final Constructor<T> constructor;

    public ConstructorObjectBuilder(Constructor<T> constructor) {
        this.constructor = constructor;
    }
    
    @Override
    public T build(Object[] tuple) {
        if(constructor.getParameterTypes().length != tuple.length){
            throw new RuntimeException("Constructor expects " + constructor.getParameterTypes().length + " arguments but " + tuple.length + " arguments were queried");
        }
        try {
            return constructor.newInstance(tuple);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<T> buildList(List<T> list) {
        return list;
    }

    @Override
    public void applySelects(SelectBuilder<?, ?> queryBuilder) {
    }
    
}
