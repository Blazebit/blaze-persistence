/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel.attribute;

import com.blazebit.persistence.view.impl.metamodel.AbstractParameterSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBuildingContext;
import com.blazebit.persistence.view.impl.metamodel.ParameterAttributeMapping;
import com.blazebit.persistence.view.metamodel.SubqueryAttribute;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SubqueryParameterSingularAttribute<X, Y> extends AbstractParameterSingularAttribute<X, Y> implements SubqueryAttribute<X, Y> {

    public SubqueryParameterSingularAttribute(MappingConstructorImpl<X> constructor, ParameterAttributeMapping mapping, MetamodelBuildingContext context) {
        super(constructor, mapping, context, null);
    }

    @Override
    public boolean isCorrelated() {
        return false;
    }
}
