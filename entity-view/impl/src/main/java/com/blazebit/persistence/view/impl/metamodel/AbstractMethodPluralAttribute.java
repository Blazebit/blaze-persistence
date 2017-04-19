/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.CollectionMapping;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.Type;

import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractMethodPluralAttribute<X, C, Y> extends AbstractMethodAttribute<X, C> implements PluralAttribute<X, C, Y> {

    private final Type<Y> elementType;
    private final Map<ManagedViewType<? extends Y>, String> elementInheritanceSubtypes;
    private final boolean sorted;
    private final boolean ordered;
    private final boolean ignoreIndex;
    private final Class<Comparator<Y>> comparatorClass;
    private final Comparator<Y> comparator;

    @SuppressWarnings("unchecked")
    public AbstractMethodPluralAttribute(ManagedViewTypeImpl<X> viewType, MethodAttributeMapping mapping, MetamodelBuildingContext context) {
        super(viewType, mapping, context);
        this.elementType = (Type<Y>) mapping.getElementType();
        this.elementInheritanceSubtypes = (Map<ManagedViewType<? extends Y>, String>) (Map<?, ?>) mapping.getElementInheritanceSubtypes();
        this.sorted = mapping.isSorted();
        
        CollectionMapping collectionMapping = mapping.getCollectionMapping();
        this.ordered = collectionMapping.ordered();
        this.ignoreIndex = collectionMapping.ignoreIndex();
        this.comparatorClass = MetamodelUtils.getComparatorClass(collectionMapping);
        this.comparator = MetamodelUtils.getComparator(comparatorClass);
    }

    @Override
    public AttributeType getAttributeType() {
        return AttributeType.PLURAL;
    }

    @Override
    public Type<Y> getElementType() {
        return elementType;
    }

    @Override
    public Map<ManagedViewType<? extends Y>, String> getElementInheritanceSubtypeMappings() {
        return elementInheritanceSubtypes;
    }

    @SuppressWarnings("unchecked")
    protected Map<ManagedViewTypeImpl<?>, String> elementInheritanceSubtypeMappings() {
        return (Map<ManagedViewTypeImpl<?>, String>) (Map<?, ?>) elementInheritanceSubtypes;
    }

    @Override
    public boolean isCollection() {
        return true;
    }

    @Override
    public boolean isSubview() {
        return elementType.getMappingType() != Type.MappingType.BASIC;
    }

    @Override
    public boolean isSorted() {
        return sorted;
    }

    @Override
    public boolean isOrdered() {
        return ordered;
    }
    
    protected boolean isIgnoreIndex() {
        return ignoreIndex;
    }

    @Override
    public Class<Comparator<Y>> getComparatorClass() {
        return comparatorClass;
    }

    @Override
    public Comparator<Y> getComparator() {
        return comparator;
    }

}
