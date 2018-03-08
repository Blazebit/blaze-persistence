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

package com.blazebit.persistence.view.impl;

import java.lang.reflect.Constructor;
import java.util.Map;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.SubqueryProvider;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ParameterizedSubqueryProviderFactory implements SubqueryProviderFactory {
    
    private final Constructor<? extends SubqueryProvider> constructor;
    private final String[] parameterNames;

    public ParameterizedSubqueryProviderFactory(Constructor<? extends SubqueryProvider> constructor, String[] parameterNames) {
        this.constructor = constructor;
        this.parameterNames = parameterNames;
    }

    @Override
    public boolean isParameterized() {
        return parameterNames.length > 0;
    }

    @Override
    public SubqueryProvider create(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters) {
        try {
            int size = parameterNames.length;
            Object[] args = new Object[size];
            
            for (int i = 0; i < size; i++) {
                final String name = parameterNames[i];
                if (parameterHolder.isParameterSet(name)) {
                    args[i] = parameterHolder.getParameterValue(name);
                } else {
                    args[i] = optionalParameters.get(name);
                }
            }
            
            return constructor.newInstance(args);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not instantiate the subquery provider: " + constructor.getDeclaringClass().getName(), ex);
        }
    }

}
