package com.blazebit.persistence.view.impl;

import java.util.Map;

import com.blazebit.persistence.CommonQueryBuilder;
import com.blazebit.persistence.view.SubqueryProvider;

/**
 * 
 * @author Christian
 * @since 1.1.0
 */
public interface SubqueryProviderFactory {
	
	public boolean isParameterized();

	public SubqueryProvider create(CommonQueryBuilder<?> queryBuilder, Map<String, Object> optionalParameters);
}
