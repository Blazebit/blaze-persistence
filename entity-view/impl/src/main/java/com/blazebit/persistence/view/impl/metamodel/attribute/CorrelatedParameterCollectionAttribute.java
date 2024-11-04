/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel.attribute;

import com.blazebit.persistence.view.impl.metamodel.EmbeddableOwner;
import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBuildingContext;
import com.blazebit.persistence.view.impl.metamodel.ParameterAttributeMapping;
import com.blazebit.persistence.view.metamodel.CorrelatedAttribute;

import java.util.Collection;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedParameterCollectionAttribute<X, Y> extends AbstractParameterCollectionAttribute<X, Y> implements CorrelatedAttribute<X, Collection<Y>> {

    public CorrelatedParameterCollectionAttribute(MappingConstructorImpl<X> mappingConstructor, ParameterAttributeMapping mapping, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        super(mappingConstructor, mapping, context, embeddableMapping);
    }

    @Override
    public boolean isCorrelated() {
        return true;
    }
}
