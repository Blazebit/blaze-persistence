/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
