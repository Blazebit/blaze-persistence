/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import com.blazebit.persistence.ParameterHolder;

import java.util.Map;

/**
 * A factory for creating a {@link SubqueryProvider}.
 * 
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface SubqueryProviderFactory {

    /**
     * Returns whether the {@link SubqueryProvider} is parameterized or not.
     *
     * @return whether the {@link SubqueryProvider} is parameterized or not
     */
    public boolean isParameterized();

    /**
     * Creates and returns a new subquery provider for the given parameters.
     *
     * @param parameterHolder The parameter holder i.e. a {@link com.blazebit.persistence.CriteriaBuilder}
     * @param optionalParameters The optional parameter map
     * @return the subquery provider
     */
    public SubqueryProvider create(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters);
}
