/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel.attribute;

import com.blazebit.persistence.view.impl.metamodel.AbstractParameterSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.EmbeddableOwner;
import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBuildingContext;
import com.blazebit.persistence.view.impl.metamodel.ParameterAttributeMapping;
import com.blazebit.persistence.view.metamodel.CorrelatedAttribute;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedParameterSingularAttribute<X, Y> extends AbstractParameterSingularAttribute<X, Y> implements CorrelatedAttribute<X, Y> {

    public CorrelatedParameterSingularAttribute(MappingConstructorImpl<X> constructor, ParameterAttributeMapping mapping, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        super(constructor, mapping, context, embeddableMapping);
    }

    @Override
    public boolean isCorrelated() {
        return true;
    }
}
