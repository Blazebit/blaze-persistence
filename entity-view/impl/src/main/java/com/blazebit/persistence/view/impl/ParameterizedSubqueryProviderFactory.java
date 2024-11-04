/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

import java.lang.reflect.Constructor;
import java.util.Map;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.SubqueryProviderFactory;

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
