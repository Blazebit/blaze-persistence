/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.IdentifiableType;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TransientEntityAssociationParameterTransformerFactory implements AssociationParameterTransformerFactory {

    private final EntityMetamodel metamodel;
    private final AssociationToIdParameterTransformer toIdParameterTransformer;

    public TransientEntityAssociationParameterTransformerFactory(EntityMetamodel metamodel, AssociationToIdParameterTransformer toIdParameterTransformer) {
        this.metamodel = metamodel;
        this.toIdParameterTransformer = toIdParameterTransformer;
    }

    @Override
    public ParameterValueTransformer getToEntityTranformer(Class<?> entityType) {
        IdentifiableType<?> managedType = (IdentifiableType<?>) metamodel.getManagedType(entityType);
        Attribute<?, ?> idAttribute = JpaMetamodelUtils.getSingleIdAttribute(managedType);
        return AssociationFromIdParameterTransformer.getInstance(entityType, idAttribute);
    }

    @Override
    public ParameterValueTransformer getToIdTransformer() {
        return toIdParameterTransformer;
    }
}
