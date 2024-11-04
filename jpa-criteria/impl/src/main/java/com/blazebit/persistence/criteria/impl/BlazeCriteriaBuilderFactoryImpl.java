/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.criteria.BlazeCriteriaBuilder;
import com.blazebit.persistence.criteria.spi.BlazeCriteriaBuilderFactory;

/**
 * @author Christian Beikov
 * @since 1.2.1
 */
@ServiceProvider(BlazeCriteriaBuilderFactory.class)
public class BlazeCriteriaBuilderFactoryImpl implements BlazeCriteriaBuilderFactory {

    @Override
    public BlazeCriteriaBuilder createCriteriaBuilder(CriteriaBuilderFactory criteriaBuilderFactory) {
        return new BlazeCriteriaBuilderImpl(criteriaBuilderFactory);
    }
}