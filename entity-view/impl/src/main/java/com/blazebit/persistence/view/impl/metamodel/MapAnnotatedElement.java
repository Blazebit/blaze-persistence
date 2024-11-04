/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class MapAnnotatedElement implements AnnotatedElement {

    private final Map<Class<?>, Annotation> annotationMap;

    public MapAnnotatedElement(Map<Class<?>, Annotation> annotationMap) {
        this.annotationMap = annotationMap;
    }

    public MapAnnotatedElement(Annotation[] annotations) {
        Map<Class<?>, Annotation> parameterAnnotations = new HashMap<>(annotations.length);
        for (Annotation annotation : annotations) {
            parameterAnnotations.put(annotation.annotationType(), annotation);
        }
        this.annotationMap = parameterAnnotations;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return annotationClass.cast(annotationMap.get(annotationClass));
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotationMap.values().toArray(new Annotation[0]);
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return annotationMap.values().toArray(new Annotation[0]);
    }
}
