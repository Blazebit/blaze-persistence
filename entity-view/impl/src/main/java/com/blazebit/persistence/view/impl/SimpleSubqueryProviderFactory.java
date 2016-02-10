package com.blazebit.persistence.view.impl;

import java.util.Map;

import com.blazebit.persistence.CommonQueryBuilder;
import com.blazebit.persistence.view.SubqueryProvider;

public class SimpleSubqueryProviderFactory implements SubqueryProviderFactory {
	
	private final Class<? extends SubqueryProvider> clazz;

	public SimpleSubqueryProviderFactory(Class<? extends SubqueryProvider> clazz) {
		this.clazz = clazz;
	}

	@Override
	public boolean isParameterized() {
		return false;
	}

	@Override
	public SubqueryProvider create(CommonQueryBuilder<?> queryBuilder, Map<String, Object> optionalParameters) {
		try {
			return clazz.newInstance();
	    } catch (Exception ex) {
	        throw new IllegalArgumentException("Could not instantiate the subquery provider: " + clazz.getName(), ex);
	    }
	}

}
