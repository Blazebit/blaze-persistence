package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.CommonQueryBuilder;
import com.blazebit.persistence.view.CorrelationProvider;

import java.util.Map;

/**
 * 
 * @author Christian
 * @since 1.2.0
 */
public interface CorrelationProviderFactory {
    
    public boolean isParameterized();

    public CorrelationProvider create(CommonQueryBuilder<?> queryBuilder, Map<String, Object> optionalParameters);
}
