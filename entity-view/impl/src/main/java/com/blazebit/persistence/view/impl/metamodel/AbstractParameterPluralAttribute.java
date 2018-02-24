/*
 * Copyright 2014 - 2018 Blazebit.
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

import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.Type;

import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractParameterPluralAttribute<X, C, Y> extends AbstractParameterAttribute<X, C> implements PluralAttribute<X, C, Y>, ParameterAttribute<X, C> {

    private final Type<Y> elementType;
    private final Map<ManagedViewType<? extends Y>, String> elementInheritanceSubtypes;
    private final boolean sorted;
    private final boolean ordered;
    private final Class<Comparator<Object>> comparatorClass;
    private final Comparator<Object> comparator;

    @SuppressWarnings("unchecked")
    public AbstractParameterPluralAttribute(MappingConstructorImpl<X> mappingConstructor, ParameterAttributeMapping mapping, MetamodelBuildingContext context) {
        super(mappingConstructor, mapping, context);
        this.elementType = (Type<Y>) mapping.getElementType(context);
        this.elementInheritanceSubtypes = (Map<ManagedViewType<? extends Y>, String>) (Map<?, ?>) mapping.getElementInheritanceSubtypes(context);
        this.sorted = mapping.isSorted();

        this.ordered = mapping.getContainerBehavior() == AttributeMapping.ContainerBehavior.ORDERED;
        this.comparatorClass = (Class<Comparator<Object>>) mapping.getComparatorClass();
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
    protected Map<ManagedViewTypeImplementor<?>, String> elementInheritanceSubtypeMappings() {
        return (Map<ManagedViewTypeImplementor<?>, String>) (Map<?, ?>) elementInheritanceSubtypes;
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
    
    @Override
    @SuppressWarnings("unchecked")
    public Class<Comparator<?>> getComparatorClass() {
        return (Class<Comparator<?>>) (Class<?>) comparatorClass;
    }

    @Override
    public Comparator<?> getComparator() {
        return comparator;
    }
}
