package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.CommonQueryBuilder;
import com.blazebit.persistence.view.CorrelationProvider;

import java.lang.reflect.Constructor;
import java.util.Map;

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
    public CorrelationProvider create(CommonQueryBuilder<?> queryBuilder, Map<String, Object> optionalParameters) {
        try {
            int size = parameterNames.length;
            Object[] args = new Object[size];
            
            for (int i = 0; i < size; i++) {
                final String name = parameterNames[i];
                if (queryBuilder.isParameterSet(name)) {
                    args[i] = queryBuilder.getParameterValue(name);
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
