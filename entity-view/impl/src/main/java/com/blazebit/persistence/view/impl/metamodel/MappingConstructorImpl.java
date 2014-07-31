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

import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.ViewConstructor;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author cpbec
 */
public class MappingConstructorImpl<X> implements MappingConstructor<X> {
    
    private final String name;
    private final ViewType<X> declaringType;
    private final Constructor<X> javaConstructor;
    private final List<ParameterAttribute<? super X, ?>> parameters;
    
    public MappingConstructorImpl(ViewType<X> viewType, String name, Constructor<X> constructor) {
        this.name = name;
        this.declaringType = viewType;
        this.javaConstructor = constructor;
        
        int parameterCount = constructor.getParameterTypes().length;
        List<ParameterAttribute<? super X, ?>> parameters = new ArrayList<ParameterAttribute<? super X, ?>>(parameterCount);
        for (int i = 0; i < parameterCount; i++) {
            parameters.add(createParameterAttribute(this, i));
        }
        
        this.parameters = Collections.unmodifiableList(parameters);
    }
    
    public static String validate(ViewType<?> viewType, Constructor<?> c) {
        ViewConstructor viewConstructor = c.getAnnotation(ViewConstructor.class);
        
        if (viewConstructor == null) {
            return "init";
        }
        
        return viewConstructor.value();
    }
    
    private static <X> ParameterAttribute<? super X, ?> createParameterAttribute(MappingConstructor<X> constructor, int index) {
        Annotation mapping = AbstractParameterAttribute.getMapping(constructor, index);
        if (mapping == null) {
            return null;
        }
        
        Class<?> attributeType = constructor.getJavaConstructor().getParameterTypes()[index];
        
        if (Collection.class == attributeType) {
            return new ParameterMappingCollectionAttributeImpl<X, Object>(constructor, index, mapping);
        } else if (List.class == attributeType) {
            return new ParameterMappingListAttributeImpl<X, Object>(constructor, index, mapping);
        } else if (Set.class == attributeType) {
            return new ParameterMappingSetAttributeImpl<X, Object>(constructor, index, mapping);
        } else if (Map.class == attributeType) {
            return new ParameterMappingMapAttributeImpl<X, Object, Object>(constructor, index, mapping);
        } else if (mapping instanceof MappingSubquery) {
            return new ParameterSubquerySingularAttributeImpl<X, Object>(constructor, index, mapping);
        } else {
            return new ParameterMappingSingularAttributeImpl<X, Object>(constructor, index, mapping);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ViewType<X> getDeclaringType() {
        return declaringType;
    }

    @Override
    public Constructor<X> getJavaConstructor() {
        return javaConstructor;
    }

    @Override
    public List<ParameterAttribute<? super X, ?>> getParameterAttributes() {
        return parameters;
    }

    @Override
    public ParameterAttribute<? super X, ?> getParameterAttribute(int index) {
        return parameters.get(index);
    }
}
