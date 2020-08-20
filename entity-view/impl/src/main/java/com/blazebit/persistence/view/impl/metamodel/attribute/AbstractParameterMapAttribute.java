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

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.spi.ServiceProvider;
import com.blazebit.persistence.view.impl.collection.CollectionInstantiatorImplementor;
import com.blazebit.persistence.view.impl.collection.MapInstantiatorImplementor;
import com.blazebit.persistence.view.impl.metamodel.AbstractParameterPluralAttribute;
import com.blazebit.persistence.view.impl.metamodel.EmbeddableOwner;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBuildingContext;
import com.blazebit.persistence.view.impl.metamodel.ParameterAttributeMapping;
import com.blazebit.persistence.view.impl.objectbuilder.ContainerAccumulator;
import com.blazebit.persistence.view.impl.objectbuilder.MapInstantiatorAccumulator;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.Type;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractParameterMapAttribute<X, K, V> extends AbstractParameterPluralAttribute<X, Map<K, V>, V> implements MapAttribute<X, K, V> {

    private final String keyMapping;
    private final Expression keyMappingExpression;
    private final String[] keyFetches;
    private final Type<K> keyType;
    private final Map<ManagedViewType<? extends K>, String> keyInheritanceSubtypes;
    private final boolean forcedUnique;
    private final MapInstantiatorAccumulator mapInstantiatorAccumulator;

    @SuppressWarnings("unchecked")
    public AbstractParameterMapAttribute(MappingConstructorImpl<X> mappingConstructor, ParameterAttributeMapping mapping, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        super(mappingConstructor, mapping, context, embeddableMapping);
        this.keyMapping = determineKeyMapping(mapping);
        this.keyMappingExpression = createSimpleExpression(keyMapping, mapping, context, ExpressionLocation.MAPPING_INDEX);
        if (mapping.getMappingIndex() == null) {
            this.keyFetches = EMPTY;
        } else {
            this.keyFetches = mapping.getMappingIndex().fetches();
        }
        this.keyType = (Type<K>) mapping.getKeyType(context, embeddableMapping);
        this.keyInheritanceSubtypes = (Map<ManagedViewType<? extends K>, String>) (Map<?, ?>) mapping.getKeyInheritanceSubtypes(context, embeddableMapping);
        this.forcedUnique = mapping.isForceUniqueness() || determineForcedUnique(context);
        this.mapInstantiatorAccumulator = new MapInstantiatorAccumulator(
                createMapInstantiator(context, null, isSorted(), isOrdered(), getComparator()),
                createValueContainerAccumulator(),
                !isCorrelated()
        );
    }

    @Override
    public boolean isForcedUnique() {
        return forcedUnique;
    }

    @Override
    public Type<K> getKeyType() {
        return keyType;
    }

    @Override
    public Map<ManagedViewType<? extends K>, String> getKeyInheritanceSubtypeMappings() {
        return keyInheritanceSubtypes;
    }

    @SuppressWarnings("unchecked")
    protected Map<ManagedViewTypeImplementor<?>, String> keyInheritanceSubtypeMappings() {
        return (Map<ManagedViewTypeImplementor<?>, String>) (Map<?, ?>) keyInheritanceSubtypes;
    }

    @Override
    public boolean isKeySubview() {
        return keyType.getMappingType() != Type.MappingType.BASIC;
    }

    @Override
    public CollectionType getCollectionType() {
        return CollectionType.MAP;
    }

    @Override
    public ContainerAccumulator<?> getContainerAccumulator() {
        return mapInstantiatorAccumulator;
    }

    @Override
    public CollectionInstantiatorImplementor<?, ?> getCollectionInstantiator() {
        throw new UnsupportedOperationException("Map attribute");
    }

    @Override
    public MapInstantiatorImplementor<?, ?> getMapInstantiator() {
        return mapInstantiatorAccumulator.getMapInstantiator();
    }

    @Override
    public boolean isIndexed() {
        return true;
    }

    @Override
    public String getKeyMapping() {
        return keyMapping;
    }

    @Override
    public Expression getKeyMappingExpression() {
        return keyMappingExpression;
    }

    @Override
    public String[] getKeyFetches() {
        return keyFetches;
    }

    @Override
    public void renderKeyMapping(String parent, ServiceProvider serviceProvider, StringBuilder sb) {
        renderExpression(parent, keyMappingExpression, null, serviceProvider, sb);
    }

}
