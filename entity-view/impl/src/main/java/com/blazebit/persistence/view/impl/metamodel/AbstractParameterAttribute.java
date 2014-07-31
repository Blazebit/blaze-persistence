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
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.reflection.ReflectionUtils;
import java.lang.annotation.Annotation;

/**
 *
 * @author cpbec
 */
public abstract class AbstractParameterAttribute<X, Y> implements ParameterAttribute<X, Y> {
    
    private final int index;
    private final MappingConstructor<X> declaringConstructor;
    private final Class<Y> javaType;
    private final String mapping;
    private final Class<? extends SubqueryProvider> subqueryProvider;
    private final boolean mappingParameter;
    private final boolean subqueryMapping;
    
    public AbstractParameterAttribute(MappingConstructor<X> constructor, int index, Annotation mapping) {
        this.index = index;
        this.declaringConstructor = constructor;
        this.javaType = (Class<Y>) constructor.getJavaConstructor().getParameterTypes()[index];
        
        if (mapping instanceof Mapping) {
            this.mapping = ((Mapping) mapping).value();
            this.subqueryProvider = null;
            this.mappingParameter = false;
            this.subqueryMapping = false;
        } else if (mapping instanceof MappingParameter) {
            this.mapping = ((MappingParameter) mapping).value();
            this.subqueryProvider = null;
            this.mappingParameter = true;
            this.subqueryMapping = false;
        } else if (mapping instanceof MappingSubquery) {
            this.mapping = null;
            this.subqueryProvider = ((MappingSubquery) mapping).value();
            this.mappingParameter = false;
            this.subqueryMapping = true;
        } else {
            throw new IllegalArgumentException("No mapping annotation could be found for the parameter of the constructor '" + declaringConstructor.getJavaConstructor().toString() +  "' at the index '" + index + "'!");
        }
        
        if (this.mapping != null && this.mapping.isEmpty()) {
            throw new IllegalArgumentException("Illegal empty mapping for the parameter of the constructor '" + declaringConstructor.getJavaConstructor().toString() +  "' at the index '" + index + "'!");
        }
    }
    
    public static Annotation getMapping(MappingConstructor<?> constructor, int index) {
        Annotation[] annotations = constructor.getJavaConstructor().getParameterAnnotations()[index];
        
        for (int i = 0; i < annotations.length; i++) {
            if (ReflectionUtils.isSubtype(annotations[i].annotationType(), Mapping.class)) {
                return annotations[i];
            } else if (ReflectionUtils.isSubtype(annotations[i].annotationType(), MappingParameter.class)) {
                return annotations[i];
            } else if (ReflectionUtils.isSubtype(annotations[i].annotationType(), MappingSubquery.class)) {
                return annotations[i];
            }
        }
        
        return null;
    }
    
    public PluralAttribute.CollectionType getCollectionType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getIndex() {
        return index;
    }
    
    public boolean isQueryParameter() {
        return mappingParameter;
    }

    public Class<? extends SubqueryProvider> getSubqueryProvider() {
        return subqueryProvider;
    }

    @Override
    public boolean isSubquery() {
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

    public String getMapping() {
        return mapping;
    }
    
}
