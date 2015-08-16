package com.blazebit.persistence.view.impl.proxy;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ViewType;

public class ReflectionInstantiator<T> implements ObjectInstantiator<T> {

	private final Constructor<T> constructor;

	public ReflectionInstantiator(MappingConstructor<T> mappingConstructor, ProxyFactory proxyFactory, ViewType<T> viewType, Class<?>[] parameterTypes) {
		Class<T> proxyClazz = (Class<T>) proxyFactory.getProxy(viewType);
        Constructor<T> javaConstructor = null;
        
        try {
            javaConstructor = proxyClazz.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException("The given mapping constructor '" + mappingConstructor + "' does not map to a constructor of the proxy class: " + proxyClazz
                .getName(), ex);
        } catch (SecurityException ex) {
            throw new IllegalArgumentException("The given mapping constructor '" + mappingConstructor + "' does not map to a constructor of the proxy class: " + proxyClazz
                .getName(), ex);
        }
        
        if (javaConstructor == null) {
            throw new IllegalArgumentException("The given mapping constructor '" + mappingConstructor + "' does not map to a constructor of the proxy class: " + proxyClazz
                .getName());
        }
        
        this.constructor = javaConstructor;
	}

	@Override
	public T newInstance(Object[] tuple) {
        try {
            return constructor.newInstance(tuple);
        } catch (Exception ex) {
            throw new RuntimeException("Could not invoke the proxy constructor '" + constructor + "' with the given tuple: " + Arrays.toString(tuple), ex);
        }
	}
	
}
