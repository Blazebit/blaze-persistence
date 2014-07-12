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

import com.blazebit.persistence.view.ViewConstructor;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author cpbec
 */
public class MappingConstructorImpl<X> implements MappingConstructor<X> {
    
    private final String name;
    private final ViewType<X> declaringType;
    private final Constructor<X> javaConstructor;
    private final List<ParameterAttribute<X, ?>> parameters;
    
    public MappingConstructorImpl(ViewType<X> viewType, String name, Constructor<X> constructor) {
        this.name = name;
        this.declaringType = viewType;
        this.javaConstructor = constructor;
        
        int parameterCount = constructor.getParameterTypes().length;
        List<ParameterAttribute<X, ?>> parameters = new ArrayList<ParameterAttribute<X, ?>>(parameterCount);
        for (int i = 0; i < parameterCount; i++) {
            parameters.add(new ParameterAttributeImpl<X, Object>(this, i));
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
    public List<ParameterAttribute<X, ?>> getParameterAttributes() {
        return parameters;
    }

    @Override
    public ParameterAttribute<X, ?> getParameterAttribute(int index) {
        return parameters.get(index);
    }
}
