/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

import java.util.Map;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.SubqueryProviderFactory;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
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
    public SubqueryProvider create(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters) {
        try {
            return clazz.newInstance();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not instantiate the subquery provider: " + clazz.getName(), ex);
        }
    }

}
