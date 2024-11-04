/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel.attribute;

import com.blazebit.persistence.view.impl.metamodel.EmbeddableOwner;
import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBuildingContext;
import com.blazebit.persistence.view.impl.metamodel.ParameterAttributeMapping;
import com.blazebit.persistence.view.metamodel.MappingAttribute;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class MappingParameterListAttribute<X, Y> extends AbstractParameterListAttribute<X, Y> implements MappingAttribute<X, List<Y>> {

    public MappingParameterListAttribute(MappingConstructorImpl<X> mappingConstructor, ParameterAttributeMapping mapping, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        super(mappingConstructor, mapping, context, embeddableMapping);
    }

    @Override
    public boolean isCorrelated() {
        return false;
    }
}
