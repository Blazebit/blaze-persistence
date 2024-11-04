/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.spi;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.criteria.BlazeCriteriaBuilder;

/**
 * A service provider for {@link com.blazebit.persistence.criteria.BlazeCriteriaBuilder} instances.
 *
 * @author Christian Beikov
 * @since 1.2.1
 */
public interface BlazeCriteriaBuilderFactory {

    /**
     * Creates a new {@link BlazeCriteriaBuilder} instance bound to the given criteria builder factory.
     *
     * @param criteriaBuilderFactory The criteria builder factory to which the persistence unit is bound
     * @return A new {@link BlazeCriteriaBuilder}
     */
    public BlazeCriteriaBuilder createCriteriaBuilder(CriteriaBuilderFactory criteriaBuilderFactory);

}
