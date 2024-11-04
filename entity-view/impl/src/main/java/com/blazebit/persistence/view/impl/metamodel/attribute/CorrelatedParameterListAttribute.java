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

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class CorrelatedParameterListAttribute<X, Y> extends AbstractParameterListAttribute<X, Y> implements CorrelatedAttribute<X, List<Y>> {

    public CorrelatedParameterListAttribute(MappingConstructorImpl<X> mappingConstructor, ParameterAttributeMapping mapping, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        super(mappingConstructor, mapping, context, embeddableMapping);
    }

    @Override
    public boolean isCorrelated() {
        return true;
    }
}
