package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.MappingParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

public class CorrelationProviderHelper {

    private CorrelationProviderHelper() {
    }

    @SuppressWarnings("unchecked")
    public static CorrelationProviderFactory getFactory(Class<? extends CorrelationProvider> clazz) {
        Constructor<? extends CorrelationProvider>[] constructors = (Constructor<? extends CorrelationProvider>[]) clazz.getConstructors();
        
        if (constructors.length > 1) {
            throw new IllegalArgumentException("Invalid subquery provider with more than a single constructors: " + clazz.getName());
        }
        
        Constructor<? extends CorrelationProvider> constructor = constructors[0];
        Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
        int size = parameterAnnotations.length;
        
        if (size == 0) {
            return new SimpleCorrelationProviderFactory(clazz);
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
        
        return new ParameterizedCorrelationProviderFactory(constructor, parameterNames);
    }
}
