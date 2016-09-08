package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.CommonQueryBuilder;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.SubqueryProvider;

import java.util.Map;

public class SimpleCorrelationProviderFactory implements CorrelationProviderFactory {

	private final Class<? extends CorrelationProvider> clazz;

	public SimpleCorrelationProviderFactory(Class<? extends CorrelationProvider> clazz) {
		this.clazz = clazz;
	}

	@Override
	public boolean isParameterized() {
		return false;
	}

	@Override
	public CorrelationProvider create(CommonQueryBuilder<?> queryBuilder, Map<String, Object> optionalParameters) {
		try {
			return clazz.newInstance();
	    } catch (Exception ex) {
	        throw new IllegalArgumentException("Could not instantiate the correlation provider: " + clazz.getName(), ex);
	    }
	}

}
