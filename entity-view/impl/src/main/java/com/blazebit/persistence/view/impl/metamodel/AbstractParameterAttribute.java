/*
 * Copyright 2014 - 2017 Blazebit.
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

import com.blazebit.persistence.view.BatchFetch;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.reflection.ReflectionUtils;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractParameterAttribute<X, Y> extends AbstractAttribute<X, Y> implements ParameterAttribute<X, Y> {

    private final int index;
    private final MappingConstructor<X> declaringConstructor;

    @SuppressWarnings("unchecked")
    public AbstractParameterAttribute(MappingConstructor<X> constructor, int index, Annotation mapping, MetamodelBuildingContext context) {
        super(constructor.getDeclaringType(),
              (Class<Y>) constructor.getJavaConstructor().getParameterTypes()[index],
              mapping,
              findAnnotation(constructor.getJavaConstructor().getParameterAnnotations()[index], BatchFetch.class),
              "for the parameter of the constructor '" + constructor.getJavaConstructor().toString() + "' at the index '" + index + "'!",
              context);
        this.index = index;
        this.declaringConstructor = constructor;

        if (this.mapping != null && this.mapping.isEmpty()) {
            context.addError("Illegal empty mapping for the parameter of the constructor '" + declaringConstructor.getJavaConstructor().toString()
                + "' at the index '" + index + "'!");
        }
    }

    @Override
    protected Class[] getTypeArguments() {
        Class<?> clazz = getDeclaringType().getJavaType();
        Constructor<?> constructor = getDeclaringConstructor().getJavaConstructor();
        Type[] genericParameterTypes = constructor.getGenericParameterTypes();

        return ReflectionUtils.resolveTypeArguments(clazz, genericParameterTypes[getIndex()]);
    }

    private static <T extends Annotation> T findAnnotation(Annotation[] parameterAnnotations, Class<T> annotationClass) {
        for (Annotation a : parameterAnnotations) {
            if (a.annotationType() == annotationClass) {
                return (T) a;
            }
        }

        return null;
    }

    public static void validate(MappingConstructor<?> constructor, int index, MetamodelBuildingContext context) {
        Annotation[] annotations = constructor.getJavaConstructor().getParameterAnnotations()[index];
        boolean foundAnnotation = false;
        
        for (Annotation a : annotations) {
            if (MappingParameter.class.isInstance(a)
                    || Mapping.class.isInstance(a)
                    || MappingSubquery.class.isInstance(a)
                    || MappingCorrelated.class.isInstance(a)) {
                foundAnnotation = true;
                break;
            }
        }
        
        if (!foundAnnotation) {
            context.addError("No MappingParameter annotation given for the parameter of the constructor '" + constructor.getJavaConstructor() + "' of the class '"
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
            } else if (ReflectionUtils.isSubtype(annotations[i].annotationType(), MappingCorrelated.class)) {
                return annotations[i];
            }
        }

        return null;
    }

    @Override
    protected String getLocation() {
        return "parameter with the index '" + getIndex() + "' of the constructor '" + getDeclaringConstructor().getJavaConstructor() + "'";
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
