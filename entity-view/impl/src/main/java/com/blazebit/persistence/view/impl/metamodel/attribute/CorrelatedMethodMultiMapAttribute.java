/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel.attribute;

import com.blazebit.persistence.view.impl.metamodel.EmbeddableOwner;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBuildingContext;
import com.blazebit.persistence.view.impl.metamodel.MethodAttributeMapping;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodMultiMapAttribute;

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class CorrelatedMethodMultiMapAttribute<X, K, V, C extends Collection<V>> extends AbstractMethodMapAttribute<X, K, C> implements MappingAttribute<X, Map<K, C>>, MethodMultiMapAttribute<X, K, V, C> {

    public CorrelatedMethodMultiMapAttribute(ManagedViewTypeImplementor<X> viewType, MethodAttributeMapping mapping, MetamodelBuildingContext context, int attributeIndex, int dirtyStateIndex, EmbeddableOwner embeddableMapping) {
        super(viewType, mapping, context, attributeIndex, dirtyStateIndex, embeddableMapping);
    }

    @Override
    public boolean isCorrelated() {
        return true;
    }
}
