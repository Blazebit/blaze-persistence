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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.SubqueryProvider;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SubqueryProviderHelper {

    private SubqueryProviderHelper() {
    }

    @SuppressWarnings("unchecked")
    public static SubqueryProviderFactory getFactory(Class<? extends SubqueryProvider> clazz) {
        Constructor<? extends SubqueryProvider>[] constructors = (Constructor<? extends SubqueryProvider>[]) clazz.getConstructors();
        
        if (constructors.length > 1) {
            throw new IllegalArgumentException("Invalid subquery provider with more than a single constructors: " + clazz.getName());
        }
        
        Constructor<? extends SubqueryProvider> constructor = constructors[0];
        Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
        int size = parameterAnnotations.length;
        
        if (size == 0) {
            return new SimpleSubqueryProviderFactory(clazz);
        }
        
        String[] parameterNames = new String[size];
        Annotation[] annotations;
        
        for (int i = 0; i < size; i++) {
            annotations = parameterAnnotations[i];
            int annotationsSize = annotations.length;
            
            for (int j = 0; j < annotationsSize; j++) {
                if (annotations[j].annotationType() == MappingParameter.class) {
                    parameterNames[i] = ((MappingParameter) annotations[j]).value();
                    break;
                }
            }
            
            if (parameterNames[i] == null) {
                throw new IllegalArgumentException("Could not find any parameter mapping annotations on constructor parameter at index " + i + " of subquery provider: " + clazz.getName());
            }
        }
        
        return new ParameterizedSubqueryProviderFactory(constructor, parameterNames);
    }
}
