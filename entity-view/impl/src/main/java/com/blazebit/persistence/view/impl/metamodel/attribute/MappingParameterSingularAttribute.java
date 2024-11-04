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
import com.blazebit.persistence.view.metamodel.MappingAttribute;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MappingParameterSingularAttribute<X, Y> extends AbstractParameterSingularAttribute<X, Y> implements MappingAttribute<X, Y> {

    public MappingParameterSingularAttribute(MappingConstructorImpl<X> constructor, ParameterAttributeMapping mapping, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        super(constructor, mapping, context, embeddableMapping);
    }

    @Override
    public boolean isCorrelated() {
        return false;
    }
}
