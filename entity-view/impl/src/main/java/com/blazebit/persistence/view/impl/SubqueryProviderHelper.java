package com.blazebit.persistence.view.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.SubqueryProvider;

public class SubqueryProviderHelper {

	@SuppressWarnings("unchecked")
	public static SubqueryProviderFactory getFactory(Class<? extends SubqueryProvider> clazz) {
		Constructor<? extends SubqueryProvider>[] constructors = (Constructor<? extends SubqueryProvider>[]) clazz.getConstructors();
		
		if (constructors.length > 1) {
			throw new IllegalArgumentException("Invalid subquery provider with more than a single constructors: " + clazz.getName());
		}
		
		Constructor<? extends SubqueryProvider> constructor = constructors[0]; 
		Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
		int size = parameterAnnotations.length;
		
		if (size == 0) {
			return new SimpleSubqueryProviderFactory(clazz);
		}
		
		String[] parameterNames = new String[size];
		Annotation[] annotations;
		
		for (int i = 0; i < size; i++) {
			annotations = parameterAnnotations[i];
			int annotationsSize = annotations.length;
			
			for (int j = 0; j < annotationsSize; j++) {
				if (annotations[j].annotationType() == MappingParameter.class) {
					parameterNames[i] = ((MappingParameter) annotations[j]).value(); 
					break;
				}
			}
			
			if (parameterNames[i] == null) {
				throw new IllegalArgumentException("Could not find any parameter mapping annotations on constructor parameter at index " + i + " of subquery provider: " + clazz.getName());
			}
		}
		
		return new ParameterizedSubqueryProviderFactory(constructor, parameterNames);
	}
}
