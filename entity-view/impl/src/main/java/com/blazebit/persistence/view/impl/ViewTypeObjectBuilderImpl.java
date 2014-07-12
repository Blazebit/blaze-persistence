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

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 *
 * @author cpbec
 */
public class ViewTypeObjectBuilderImpl<T> implements ObjectBuilder<T> {
    
    private final Constructor<? extends T> proxyConstructor;
    private final String[] mappings;

    public ViewTypeObjectBuilderImpl(ViewType<T> viewType, MappingConstructor<T> constructor) {
        if (constructor == null) {
            if(viewType.getConstructors().size() > 1) {
                throw new IllegalArgumentException("The given view type '" + viewType.getJavaType().getName() + "' has multiple constructors but the given constructor was null.");
            } else if (viewType.getConstructors().size() == 1) {
                constructor = (MappingConstructor<T>) viewType.getConstructors().toArray()[0];
            }
        }
        
        Object[] result = getConstructorAndMappings(ProxyFactory.getProxy(viewType), viewType, constructor);
        this.proxyConstructor = (Constructor<? extends T>) result[0];
        this.mappings = (String[]) result[1];
    }

    @Override
    public T build(Object[] tuple, String[] aliases) {
        try {
            return proxyConstructor.newInstance(tuple);
        } catch (Exception ex) {
            throw new RuntimeException("Could not invoke the proxy constructor '" + proxyConstructor + "' with the given tuple: " + Arrays.toString(tuple), ex);
        }
    }

    @Override
    public List<T> buildList(List<T> list) {
        return list;
    }
    
    @Override
    public String[] getExpressions() {
        return mappings;
    }

    private static <T> Object[] getConstructorAndMappings(Class<? extends T> proxyClass, ViewType<T> viewType, MappingConstructor<T> mappingConstructor) {
        Constructor<?>[] constructors = proxyClass.getDeclaredConstructors();
        Set<MethodAttribute<? super T, ?>> attributeSet = viewType.getAttributes();
        MethodAttribute<?, ?>[] attributes = attributeSet.toArray(new MethodAttribute<?, ?>[attributeSet.size()]);
        ParameterAttribute<?, ?>[] parameterAttributes;
        
        if (mappingConstructor == null) {
            parameterAttributes = new ParameterAttribute<?, ?>[0];
        } else {
            List<ParameterAttribute<T, ?>> parameterAttributeList = mappingConstructor.getParameterAttributes();
            parameterAttributes = parameterAttributeList.toArray(new ParameterAttribute<?, ?>[parameterAttributeList.size()]);
        }
        
        int length = attributes.length + parameterAttributes.length;
        String mappings[] = new String[length];
        
        OUTER: for (Constructor<?> constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (length != parameterTypes.length) {
                continue;
            }
            for (int i = 0; i < attributes.length; i++) {
                if (attributes[i].getJavaType() != parameterTypes[i]) {
                    continue OUTER;
                } else {
                    mappings[i] = attributes[i].getMapping();
                }
            }
            for (int i = 0; i < parameterAttributes.length; i++) {
                if (parameterAttributes[i].getJavaType() != parameterTypes[i + attributes.length]) {
                    continue OUTER;
                } else {
                    mappings[i + attributes.length] = parameterAttributes[i].getMapping();
                }
            }

            return new Object[]{ constructor, mappings };
        }
        
        throw new IllegalArgumentException("The given mapping constructor '" + mappingConstructor + "' does not map to a constructor of the proxy class: " + proxyClass.getName());
    }
}
