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

import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.reflection.ReflectionUtils;
import java.lang.annotation.Annotation;

/**
 *
 * @author cpbec
 */
public class ParameterAttributeImpl<X, Y> implements ParameterAttribute<X, Y> {
    
    private final int index;
    private final MappingConstructor<X> declaringConstructor;
    private final Class<Y> javaType;
    private final String mapping;
    private final Class<? extends SubqueryProvider> subqueryProvider;
    private final boolean mappingParameter;
    private final boolean subqueryMapping;
    
    public ParameterAttributeImpl(MappingConstructor<X> constructor, int index) {
        this.index = index;
        this.declaringConstructor = constructor;
        this.javaType = (Class<Y>) constructor.getJavaConstructor().getParameterTypes()[index];
        
        Annotation[] annotations = constructor.getJavaConstructor().getParameterAnnotations()[index];
        Mapping mappingAnnotation = null;
        MappingParameter mappingParameterAnnotation = null;
        MappingSubquery mappingSubqueryAnnotation = null;
        
        for (int i = 0; i < annotations.length; i++) {
            if (ReflectionUtils.isSubtype(annotations[i].annotationType(), Mapping.class)) {
                mappingAnnotation = (Mapping) annotations[i];
                break;
            } else if (ReflectionUtils.isSubtype(annotations[i].annotationType(), MappingParameter.class)) {
                mappingParameterAnnotation = (MappingParameter) annotations[i];
                break;
            } else if (ReflectionUtils.isSubtype(annotations[i].annotationType(), MappingSubquery.class)) {
                mappingSubqueryAnnotation = (MappingSubquery) annotations[i];
                break;
            }
        }
        
        if (mappingAnnotation != null) {
            this.mapping = mappingAnnotation.value();
            this.subqueryProvider = null;
            this.mappingParameter = false;
            this.subqueryMapping = false;
        } else if (mappingParameterAnnotation != null) {
            this.mapping = mappingParameterAnnotation.value();
            this.subqueryProvider = null;
            this.mappingParameter = true;
            this.subqueryMapping = false;
        } else if (mappingSubqueryAnnotation != null) {
            this.mapping = null;
            this.subqueryProvider = mappingSubqueryAnnotation.value();
            this.mappingParameter = false;
            this.subqueryMapping = true;
        } else {
            throw new IllegalArgumentException("No mapping annotation could be found for the parameter of the constructor '" + declaringConstructor.getJavaConstructor().toString() +  "' at the index '" + index + "'!");
        }
        
        if (mapping.isEmpty()) {
            throw new IllegalArgumentException("Illegal empty mapping for the parameter of the constructor '" + declaringConstructor.getJavaConstructor().toString() +  "' at the index '" + index + "'!");
        }
    }

    @Override
    public int getIndex() {
        return index;
    }
    
    @Override
    public boolean isMappingParameter() {
        return mappingParameter;
    }

    @Override
    public Class<? extends SubqueryProvider> getSubqueryProvider() {
        return subqueryProvider;
    }

    @Override
    public boolean isSubqueryMapping() {
        return subqueryMapping;
    }

    @Override
    public MappingConstructor<X> getDeclaringConstructor() {
        return declaringConstructor;
    }

    @Override
    public ViewType<X> getDeclaringType() {
        return declaringConstructor.getDeclaringType();
    }

    @Override
    public Class<Y> getJavaType() {
        return javaType;
    }

    @Override
    public String getMapping() {
        return mapping;
    }
    
}
