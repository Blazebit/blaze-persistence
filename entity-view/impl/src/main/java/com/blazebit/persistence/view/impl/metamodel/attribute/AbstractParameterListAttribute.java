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
import com.blazebit.persistence.view.impl.metamodel.MetamodelUtils;
import com.blazebit.persistence.view.impl.metamodel.ParameterAttributeMapping;
import com.blazebit.persistence.view.impl.objectbuilder.CollectionInstantiatorAccumulator;
import com.blazebit.persistence.view.impl.objectbuilder.ContainerAccumulator;
import com.blazebit.persistence.view.metamodel.ListAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.spi.EntityViewAttributeMapping;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractParameterListAttribute<X, Y> extends AbstractParameterPluralAttribute<X, List<Y>, Y> implements ListAttribute<X, Y> {

    private final String indexMapping;
    private final Expression indexMappingExpression;
    private final boolean isIndexed;
    private final boolean forcedUnique;
    private final boolean elementCollectionSorted;
    private final boolean elementCollectionOrdered;
    private final boolean elementCollectionForcedUnique;
    private final Class<Comparator<Object>> elementCollectionComparatorClass;
    private final Comparator<Object> elementCollectionComparator;
    private final CollectionInstantiatorAccumulator collectionInstantiatorAccumulator;
    
    public AbstractParameterListAttribute(MappingConstructorImpl<X> mappingConstructor, ParameterAttributeMapping mapping, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        super(mappingConstructor, mapping, context, embeddableMapping);
        String indexMapping = determineIndexMapping(mapping);
        if (indexMapping == null) {
            this.isIndexed = mapping.determineIndexed(context, context.getEntityMetamodel().getManagedType(mappingConstructor.getDeclaringType().getEntityClass()));
            if (this.isIndexed) {
                indexMapping = "INDEX(this)";
            }
        } else {
            this.isIndexed = true;
        }
        this.indexMapping = indexMapping;
        this.indexMappingExpression = createSimpleExpression(indexMapping, mapping, context, ExpressionLocation.MAPPING_INDEX);
        this.forcedUnique = mapping.isForceUniqueness() || determineForcedUnique(context);
        this.elementCollectionSorted = mapping.getElementCollectionBehavior() == EntityViewAttributeMapping.ElementCollectionBehavior.SORTED || getElementCollectionType() == ElementCollectionType.SORTED_SET;
        this.elementCollectionOrdered = mapping.getElementCollectionBehavior() == EntityViewAttributeMapping.ElementCollectionBehavior.ORDERED || getElementCollectionType() == ElementCollectionType.LIST;
        this.elementCollectionComparatorClass = (Class<Comparator<Object>>) mapping.getElementCollectionComparatorClass();
        this.elementCollectionComparator = MetamodelUtils.getComparator(elementCollectionComparatorClass);
        this.elementCollectionForcedUnique = mapping.isElementCollectionForceUniqueness();
        this.collectionInstantiatorAccumulator = new CollectionInstantiatorAccumulator(
                createCollectionInstantiator(context, null, isIndexed(), isSorted(), isOrdered(), getComparator()),
                createValueContainerAccumulator(elementCollectionComparator),
                isFilterNulls()
        );
    }

    @Override
    public CollectionType getCollectionType() {
        return CollectionType.LIST;
    }

    @Override
    public ContainerAccumulator<?> getContainerAccumulator() {
        return collectionInstantiatorAccumulator;
    }

    @Override
    public CollectionInstantiatorImplementor<?, ?> getCollectionInstantiator() {
        return collectionInstantiatorAccumulator.getCollectionInstantiator();
    }

    @Override
    public MapInstantiatorImplementor<?, ?> getMapInstantiator() {
        throw new UnsupportedOperationException("Collection attribute");
    }

    @Override
    public boolean isSorted() {
        return false;
    }

    @Override
    public boolean isIndexed() {
        return isIndexed;
    }

    @Override
    public boolean isOrdered() {
        return !isIndexed;
    }

    @Override
    public boolean isForcedUnique() {
        return forcedUnique;
    }

    @Override
    public boolean isElementCollectionOrdered() {
        return elementCollectionOrdered;
    }

    @Override
    public boolean isElementCollectionSorted() {
        return elementCollectionSorted;
    }

    @Override
    public boolean isElementCollectionForcedUnique() {
        return elementCollectionForcedUnique;
    }

    @Override
    public Comparator<?> getElementCollectionComparator() {
        return elementCollectionComparator;
    }

    @Override
    public Class<Comparator<?>> getElementCollectionComparatorClass() {
        return (Class<Comparator<?>>) (Class<?>) elementCollectionComparatorClass;
    }

    @Override
    protected Type<?> getKeyType() {
        return null;
    }

    @Override
    protected Map<ManagedViewTypeImplementor<?>, String> keyInheritanceSubtypeMappings() {
        return null;
    }

    @Override
    protected boolean isKeySubview() {
        return false;
    }

    @Override
    public String getIndexMapping() {
        return indexMapping;
    }

    @Override
    public Expression getMappingIndexExpression() {
        return indexMappingExpression;
    }

    @Override
    public void renderIndexMapping(String parent, ServiceProvider serviceProvider, StringBuilder sb) {
        renderExpression(parent, indexMappingExpression, null, serviceProvider, sb);
    }

}
