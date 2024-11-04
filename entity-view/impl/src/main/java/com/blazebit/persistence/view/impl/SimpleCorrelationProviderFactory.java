/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.CorrelationProviderFactory;

import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
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
    public CorrelationProvider create(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters) {
        try {
            return clazz.newInstance();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not instantiate the correlation provider: " + clazz.getName(), ex);
        }
    }

}
