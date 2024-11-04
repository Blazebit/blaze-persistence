/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ManagedEntityAssociationParameterTransformerFactory implements AssociationParameterTransformerFactory {

    private final EntityManager em;
    private final ParameterValueTransformer toIdParameterTransformer;

    public ManagedEntityAssociationParameterTransformerFactory(EntityManager em, ParameterValueTransformer toIdParameterTransformer) {
        this.em = em;
        this.toIdParameterTransformer = toIdParameterTransformer;
    }

    @Override
    public ParameterValueTransformer getToEntityTranformer(final Class<?> entityType) {
        return new ParameterValueTransformer() {

            @Override
            public ParameterValueTransformer forQuery(Query query) {
                return this;
            }

            @Override
            public Object transform(Object originalValue) {
                return em.getReference(entityType, originalValue);
            }
        };
    }

    @Override
    public ParameterValueTransformer getToIdTransformer() {
        return toIdParameterTransformer;
    }
}
