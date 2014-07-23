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

import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

/**
 *
 * @author cpbec
 */
public class ViewTypeObjectBuilderTemplate<T> {
    
    private final Constructor<? extends T> proxyConstructor;
    private final String[] mappings;
    private final String[] parameterMappings;
    private final boolean hasParameters;
    
    public static class Key<T> {
        private final ViewType<T> viewType;
        private final MappingConstructor<T> constructor;

        public Key(ViewType<T> viewType, MappingConstructor<T> constructor) {
            this.viewType = viewType;
            this.constructor = constructor;
        }
        
        public ViewTypeObjectBuilderTemplate<T> createValue(ProxyFactory proxyFactory) {
            return new ViewTypeObjectBuilderTemplate<T>(viewType, constructor, proxyFactory);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + (this.viewType != null ? this.viewType.hashCode() : 0);
            hash = 83 * hash + (this.constructor != null ? this.constructor.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key<?> other = (Key<?>) obj;
            if (this.viewType != other.viewType && (this.viewType == null || !this.viewType.equals(other.viewType))) {
                return false;
            }
            if (this.constructor != other.constructor && (this.constructor == null || !this.constructor.equals(other.constructor))) {
                return false;
            }
            return true;
        }
    }

    private ViewTypeObjectBuilderTemplate(ViewType<T> viewType, MappingConstructor<T> constructor, ProxyFactory proxyFactory) {
        if (constructor == null) {
            if(viewType.getConstructors().size() > 1) {
                throw new IllegalArgumentException("The given view type '" + viewType.getJavaType().getName() + "' has multiple constructors but the given constructor was null.");
            } else if (viewType.getConstructors().size() == 1) {
                constructor = (MappingConstructor<T>) viewType.getConstructors().toArray()[0];
            }
        }
        
        Object[] result = getConstructorAndMappings(proxyFactory.getProxy(viewType), viewType, constructor);
        this.proxyConstructor = (Constructor<? extends T>) result[0];
        this.mappings = (String[]) result[1];
        this.parameterMappings = (String[]) result[2];
        boolean parameterFound = false;
        
        for (int i = 0; i < parameterMappings.length; i++) {
            if (parameterMappings[i] != null) {
                parameterFound = true;
            }
        }
        
        this.hasParameters = parameterFound;
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
        String parameterMappings[] = new String[length];
        
        OUTER: for (Constructor<?> constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (length != parameterTypes.length) {
                continue;
            }
            for (int i = 0; i < attributes.length; i++) {
                if (attributes[i].getJavaType() != parameterTypes[i]) {
                    continue OUTER;
                } else {
                    if (attributes[i].isMappingParameter()) {
                        mappings[i] = "NULLIF(1,1)";
                        parameterMappings[i + attributes.length] = attributes[i].getMapping();
                    } else {
                        mappings[i] = attributes[i].getMapping();
                    }
                }
            }
            for (int i = 0; i < parameterAttributes.length; i++) {
                if (parameterAttributes[i].getJavaType() != parameterTypes[i + attributes.length]) {
                    continue OUTER;
                } else {
                    if (parameterAttributes[i].isMappingParameter()) {
                        mappings[i + attributes.length] = "NULLIF(1,1)";
                        parameterMappings[i + attributes.length] = parameterAttributes[i].getMapping();
                    } else {
                        mappings[i + attributes.length] = parameterAttributes[i].getMapping();
                    }
                }
            }

            return new Object[]{ constructor, mappings, parameterMappings };
        }
        
        throw new IllegalArgumentException("The given mapping constructor '" + mappingConstructor + "' does not map to a constructor of the proxy class: " + proxyClass.getName());
    }

    public Constructor<? extends T> getProxyConstructor() {
        return proxyConstructor;
    }

    public String[] getMappings() {
        return mappings;
    }

    public String[] getParameterMappings() {
        return parameterMappings;
    }

    public boolean hasParameters() {
        return hasParameters;
    }
}
