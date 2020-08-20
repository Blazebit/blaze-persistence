/*
 * Copyright 2014 - 2020 Blazebit.
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
