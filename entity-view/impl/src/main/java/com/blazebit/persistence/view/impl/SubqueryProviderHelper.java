/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.SubqueryProviderFactory;

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
        Constructor<? extends SubqueryProvider> noArgsConstructor = null;
        ParameterizedSubqueryProviderFactory parameterizedSubqueryProviderFactory = null;
        for (int constructorIdx = 0; constructorIdx < constructors.length; constructorIdx++) {
            Constructor<? extends SubqueryProvider> constructor = constructors[constructorIdx];
            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            int size = parameterAnnotations.length;

            if (size == 0) {
                noArgsConstructor = constructor;
            } else {
                String[] parameterNames = new String[size];
                Annotation[] annotations;

                boolean hasMappingParameter = false;
                int firstParameterIdxWithoutMappingParameter = -1;
                for (int i = 0; i < size; i++) {
                    annotations = parameterAnnotations[i];
                    int annotationsSize = annotations.length;

                    for (int j = 0; j < annotationsSize; j++) {
                        if (annotations[j].annotationType() == MappingParameter.class) {
                            hasMappingParameter = true;
                            parameterNames[i] = ((MappingParameter) annotations[j]).value();
                            break;
                        }
                    }

                    if (parameterNames[i] == null) {
                        firstParameterIdxWithoutMappingParameter = i;
                    }
                }
                if (hasMappingParameter) {
                    if (firstParameterIdxWithoutMappingParameter >= 0) {
                        throw new IllegalArgumentException("Could not find any parameter mapping annotations on constructor parameter at index "
                                + firstParameterIdxWithoutMappingParameter + " of subquery provider: " + clazz.getName());
                    } else {
                        parameterizedSubqueryProviderFactory = new ParameterizedSubqueryProviderFactory(constructor, parameterNames);
                    }
                }
            }
        }

        if (parameterizedSubqueryProviderFactory != null) {
            return parameterizedSubqueryProviderFactory;
        } else if (noArgsConstructor != null) {
            return new SimpleSubqueryProviderFactory(clazz);
        } else {
            throw new IllegalArgumentException("No eligible constructor exists for subquery provider: " + clazz.getName());
        }
    }
}
