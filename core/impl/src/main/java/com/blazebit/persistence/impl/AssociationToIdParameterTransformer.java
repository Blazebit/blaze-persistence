/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.spi.JpaProvider;

import jakarta.persistence.Query;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AssociationToIdParameterTransformer implements ParameterValueTransformer {

    private final JpaProvider jpaProvider;

    public AssociationToIdParameterTransformer(JpaProvider jpaProvider) {
        this.jpaProvider = jpaProvider;
    }

    @Override
    public ParameterValueTransformer forQuery(Query query) {
        return this;
    }

    @Override
    public Object transform(Object originalValue) {
        return jpaProvider.getIdentifier(originalValue);
    }

}
