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
package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.reflection.ReflectionUtils;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractParameterAttribute<X, Y> extends AbstractAttribute<X, Y> implements ParameterAttribute<X, Y> {

    private final int index;
    private final MappingConstructor<X> declaringConstructor;

    public AbstractParameterAttribute(MappingConstructor<X> constructor, int index, Annotation mapping, Set<Class<?>> entityViews) {
        super(constructor.getDeclaringType(),
              (Class<Y>) constructor.getJavaConstructor().getParameterTypes()[index],
              mapping,
              entityViews,
              "for the parameter of the constructor '" + constructor.getJavaConstructor().toString() + "' at the index '" + index + "'!");
        this.index = index;
        this.declaringConstructor = constructor;

        if (this.mapping != null && this.mapping.isEmpty()) {
            throw new IllegalArgumentException("Illegal empty mapping for the parameter of the constructor '" + declaringConstructor.getJavaConstructor().toString()
                + "' at the index '" + index + "'!");
        }
    }

    public static void validate(MappingConstructor<?> constructor, int index) {
        Class<?> type = constructor.getJavaConstructor().getParameterTypes()[index];

        if (type.isPrimitive()) {
            throw new IllegalArgumentException("Primitive type not allowed for the parameter of the constructor '" + constructor.getJavaConstructor() + "' of the class '"
                + constructor.getDeclaringType().getJavaType().getName() + "' at index '" + index + "'.");
        }
        
        Annotation[] annotations = constructor.getJavaConstructor().getParameterAnnotations()[index];
        boolean foundAnnotation = false;
        
        for (Annotation a : annotations) {
            if (MappingParameter.class.isInstance(a)) {
                foundAnnotation = true;
                break;
            }
        }
        
        if (!foundAnnotation) {
            throw new IllegalArgumentException("No MappingParameter annotation given for the parameter of the constructor '" + constructor.getJavaConstructor() + "' of the class '"
                + constructor.getDeclaringType().getJavaType().getName() + "' at index '" + index + "'.");
        }
    }

    public static Annotation getMapping(MappingConstructor<?> constructor, int index) {
        Annotation[] annotations = constructor.getJavaConstructor().getParameterAnnotations()[index];

        for (int i = 0; i < annotations.length; i++) {
            if (ReflectionUtils.isSubtype(annotations[i].annotationType(), IdMapping.class)) {
                return annotations[i];
            } else if (ReflectionUtils.isSubtype(annotations[i].annotationType(), Mapping.class)) {
                return annotations[i];
            } else if (ReflectionUtils.isSubtype(annotations[i].annotationType(), MappingParameter.class)) {
                return annotations[i];
            } else if (ReflectionUtils.isSubtype(annotations[i].annotationType(), MappingSubquery.class)) {
                return annotations[i];
            }
        }

        return null;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public MappingConstructor<X> getDeclaringConstructor() {
        return declaringConstructor;
    }

}
