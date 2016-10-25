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
package com.blazebit.persistence.view.impl.proxy;

import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingConstructor;

/**
 *
 * @author Christian Beikov
 * @since 1.0.6
 */
public class UnsafeInstantiator<T> extends ReflectionInstantiator<T> {

	public UnsafeInstantiator(MappingConstructor<T> mappingConstructor, ProxyFactory proxyFactory, ManagedViewType<T> viewType, Class<?>[] parameterTypes) {
		super(mappingConstructor, proxyFactory, viewType, parameterTypes);
	}

	@Override
	@SuppressWarnings("unchecked")
    protected Class<T> getProxyClass(ProxyFactory proxyFactory, ManagedViewType<T> viewType) {
		return (Class<T>) proxyFactory.getUnsafeProxy(viewType);
	}
	
}
