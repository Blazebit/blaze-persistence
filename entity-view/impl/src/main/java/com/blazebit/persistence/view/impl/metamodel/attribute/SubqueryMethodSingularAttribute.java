/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel.attribute;

import com.blazebit.persistence.view.impl.metamodel.AbstractMethodSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBuildingContext;
import com.blazebit.persistence.view.impl.metamodel.MethodAttributeMapping;
import com.blazebit.persistence.view.metamodel.SubqueryAttribute;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SubqueryMethodSingularAttribute<X, Y> extends AbstractMethodSingularAttribute<X, Y> implements SubqueryAttribute<X, Y> {

    public SubqueryMethodSingularAttribute(ManagedViewTypeImplementor<X> viewType, MethodAttributeMapping mapping, MetamodelBuildingContext context, int attributeIndex, int dirtyStateIndex) {
        super(viewType, mapping, context, attributeIndex, dirtyStateIndex, null);
    }

    @Override
    public boolean isCorrelated() {
        return false;
    }
}
