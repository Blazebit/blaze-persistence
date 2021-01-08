/*
 * Copyright 2014 - 2021 Blazebit.
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
