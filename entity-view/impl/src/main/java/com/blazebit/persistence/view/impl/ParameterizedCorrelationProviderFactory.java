/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.CorrelationProviderFactory;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ParameterizedCorrelationProviderFactory implements CorrelationProviderFactory {

    private final Constructor<? extends CorrelationProvider> constructor;
    private final String[] parameterNames;

    public ParameterizedCorrelationProviderFactory(Constructor<? extends CorrelationProvider> constructor, String[] parameterNames) {
        this.constructor = constructor;
        this.parameterNames = parameterNames;
    }

    @Override
    public boolean isParameterized() {
        return parameterNames.length > 0;
    }

    @Override
    public CorrelationProvider create(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters) {
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
            throw new IllegalArgumentException("Could not instantiate the correlation provider: " + constructor.getDeclaringClass().getName(), ex);
        }
    }

}
