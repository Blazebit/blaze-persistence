package com.blazebit.persistence.view.impl.proxy;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ViewType;

public class UnsafeInstantiator<T> implements ObjectInstantiator<T> {

	private final Constructor<? extends T> constructor;

	public UnsafeInstantiator(MappingConstructor<T> mappingConstructor, ProxyFactory proxyFactory, ViewType<T> viewType, Class<?>[] parameterTypes) {
		Class<T> clazz = (Class<T>) proxyFactory.getProxy(viewType);
		Constructor<T> javaConstructor = null;
        
        try {
            javaConstructor = (Constructor<T>) clazz.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException("The given mapping constructor '" + mappingConstructor + "' does not map to a constructor of the proxy class: " + clazz
                .getName(), ex);
        } catch (SecurityException ex) {
            throw new IllegalArgumentException("The given mapping constructor '" + mappingConstructor + "' does not map to a constructor of the proxy class: " + clazz
                .getName(), ex);
        }
        
        if (javaConstructor == null) {
            throw new IllegalArgumentException("The given mapping constructor '" + mappingConstructor + "' does not map to a constructor of the proxy class: " + clazz
                .getName());
        }
        
        Class<?> unsafeProxy = proxyFactory.getUnsafeProxy(viewType);
        Constructor<?> unsafeConstructor = null;
        
        try {
	        unsafeConstructor = unsafeProxy.getDeclaredConstructor(parameterTypes);
	    } catch (NoSuchMethodException ex) {
	        throw new IllegalArgumentException("Could not retrieve the unsafe proxy constructor for the proxy class: " + clazz
	            .getName(), ex);
	    } catch (SecurityException ex) {
	        throw new IllegalArgumentException("Could not retrieve the unsafe proxy constructor for the proxy class: " + clazz
		            .getName(), ex);
	    }
        
        this.constructor = SunReflectionFactoryHelper.newConstructorForSerialization(clazz, unsafeConstructor);
        this.constructor.setAccessible(true);
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
