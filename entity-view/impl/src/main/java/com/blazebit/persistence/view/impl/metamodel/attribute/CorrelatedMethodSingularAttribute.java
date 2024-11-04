/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel.attribute;

import com.blazebit.persistence.view.impl.metamodel.AbstractMethodSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.EmbeddableOwner;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBuildingContext;
import com.blazebit.persistence.view.impl.metamodel.MethodAttributeMapping;
import com.blazebit.persistence.view.metamodel.CorrelatedAttribute;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedMethodSingularAttribute<X, Y> extends AbstractMethodSingularAttribute<X, Y> implements CorrelatedAttribute<X, Y> {

    public CorrelatedMethodSingularAttribute(ManagedViewTypeImplementor<X> viewType, MethodAttributeMapping mapping, MetamodelBuildingContext context, int attributeIndex, int dirtyStateIndex, EmbeddableOwner embeddableMapping) {
        super(viewType, mapping, context, attributeIndex, dirtyStateIndex, embeddableMapping);
    }

    @Override
    public boolean isCorrelated() {
        return true;
    }
}
