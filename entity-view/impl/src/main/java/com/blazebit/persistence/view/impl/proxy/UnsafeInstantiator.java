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

package com.blazebit.persistence.view.impl.proxy;

import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingConstructor;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0.6
 */
public class UnsafeInstantiator<T> extends ReflectionInstantiator<T> {

    public UnsafeInstantiator(MappingConstructor<T> mappingConstructor, ProxyFactory proxyFactory, ManagedViewType<T> viewType, ManagedViewTypeImplementor<T> viewTypeBase, Class<?>[] parameterTypes, List<MutableBasicUserTypeEntry> mutableBasicUserTypes, List<TypeConverterEntry> typeConverterEntries) {
        super(mappingConstructor, proxyFactory, viewType, viewTypeBase, parameterTypes, mutableBasicUserTypes, typeConverterEntries);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<T> getProxyClass(ProxyFactory proxyFactory, ManagedViewType<T> viewType, ManagedViewTypeImplementor<T> viewTypeBase) {
        return (Class<T>) proxyFactory.getUnsafeProxy(viewType, viewTypeBase);
    }
    
}
