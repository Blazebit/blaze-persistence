/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel.attribute;

import com.blazebit.persistence.view.impl.collection.CollectionInstantiatorImplementor;
import com.blazebit.persistence.view.impl.collection.MapInstantiatorImplementor;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodPluralAttribute;
import com.blazebit.persistence.view.impl.metamodel.EmbeddableOwner;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBuildingContext;
import com.blazebit.persistence.view.impl.metamodel.MethodAttributeMapping;
import com.blazebit.persistence.view.impl.objectbuilder.CollectionInstantiatorAccumulator;
import com.blazebit.persistence.view.impl.objectbuilder.ContainerAccumulator;
import com.blazebit.persistence.view.metamodel.MethodCollectionAttribute;
import com.blazebit.persistence.view.metamodel.Type;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractMethodCollectionAttribute<X, Y> extends AbstractMethodPluralAttribute<X, Collection<Y>, Y> implements MethodCollectionAttribute<X, Y> {

    private final boolean forcedUnique;
    private final CollectionInstantiatorAccumulator collectionInstantiatorAccumulator;

    public AbstractMethodCollectionAttribute(ManagedViewTypeImplementor<X> viewType, MethodAttributeMapping mapping, MetamodelBuildingContext context, int attributeIndex, int dirtyStateIndex, EmbeddableOwner embeddableMapping) {
        super(viewType, mapping, context, attributeIndex, dirtyStateIndex, embeddableMapping);
        this.forcedUnique = mapping.isForceUniqueness() || determineForcedUnique(context);
        this.collectionInstantiatorAccumulator = new CollectionInstantiatorAccumulator(
                createCollectionInstantiator(context, createCollectionFactory(context), isIndexed(), isSorted(), isOrdered(), getComparator()),
                null,
                isFilterNulls()
        );
    }

    @Override
    public CollectionType getCollectionType() {
        return CollectionType.COLLECTION;
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
    public boolean isIndexed() {
        return false;
    }

    @Override
    public boolean isSorted() {
        return false;
    }
    
    @Override
    public boolean isOrdered() {
        return true;
    }

    @Override
    public boolean isForcedUnique() {
        return forcedUnique;
    }

    @Override
    public boolean isElementCollectionOrdered() {
        return false;
    }

    @Override
    public boolean isElementCollectionSorted() {
        return false;
    }

    @Override
    public boolean isElementCollectionForcedUnique() {
        return false;
    }

    @Override
    public Comparator<?> getElementCollectionComparator() {
        return null;
    }

    @Override
    public Class<Comparator<?>> getElementCollectionComparatorClass() {
        return null;
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

}
