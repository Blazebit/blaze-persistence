/*
 * Copyright 2014 - 2023 Blazebit.
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

import com.blazebit.persistence.view.impl.collection.CollectionInstantiatorImplementor;
import com.blazebit.persistence.view.impl.collection.MapInstantiatorImplementor;
import com.blazebit.persistence.view.impl.objectbuilder.ContainerAccumulator;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.Type;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractParameterSingularAttribute<X, Y> extends AbstractParameterAttribute<X, Y> implements SingularAttribute<X, Y> {

    private final Type<Y> type;
    private final Map<ManagedViewType<? extends Y>, String> inheritanceSubtypes;
    private final boolean createEmptyFlatView;

    @SuppressWarnings("unchecked")
    public AbstractParameterSingularAttribute(MappingConstructorImpl<X> constructor, ParameterAttributeMapping mapping, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        super(constructor, mapping, context, embeddableMapping);
        this.type = (Type<Y>) mapping.getType(context, embeddableMapping);
        this.inheritanceSubtypes = (Map<ManagedViewType<? extends Y>, String>) (Map<?, ?>) mapping.getInheritanceSubtypes(context, embeddableMapping);
        if (type.getMappingType() == Type.MappingType.FLAT_VIEW && (mapping.getCreateEmptyFlatViews() == null && context.isCreateEmptyFlatViews() || Boolean.TRUE.equals(mapping.getCreateEmptyFlatViews()))) {
            this.createEmptyFlatView = true;
        } else {
            this.createEmptyFlatView = false;
        }
    }

    @Override
    public boolean isCreateEmptyFlatView() {
        return createEmptyFlatView;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    protected boolean isSorted() {
        return false;
    }

    @Override
    public boolean isIndexed() {
        return false;
    }

    @Override
    protected boolean isForcedUnique() {
        return false;
    }

    @Override
    protected boolean isElementCollectionOrdered() {
        return false;
    }

    @Override
    protected boolean isElementCollectionSorted() {
        return false;
    }

    @Override
    protected boolean isElementCollectionForcedUnique() {
        return false;
    }

    @Override
    public ContainerAccumulator<?> getContainerAccumulator() {
        throw new UnsupportedOperationException("Singular attribute");
    }

    @Override
    protected PluralAttribute.CollectionType getCollectionType() {
        throw new UnsupportedOperationException("Singular attribute");
    }

    @Override
    protected PluralAttribute.ElementCollectionType getElementCollectionType() {
        throw new UnsupportedOperationException("Singular attribute");
    }

    @Override
    public CollectionInstantiatorImplementor<?, ?> getCollectionInstantiator() {
        throw new UnsupportedOperationException("Singular attribute");
    }

    @Override
    public MapInstantiatorImplementor<?, ?> getMapInstantiator() {
        throw new UnsupportedOperationException("Singular attribute");
    }

    @Override
    public AttributeType getAttributeType() {
        return AttributeType.SINGULAR;
    }

    @Override
    public Type<Y> getType() {
        return type;
    }

    @Override
    public Type<?> getElementType() {
        return type;
    }

    @Override
    public Map<ManagedViewType<? extends Y>, String> getInheritanceSubtypeMappings() {
        return inheritanceSubtypes;
    }

    @SuppressWarnings("unchecked")
    protected Map<ManagedViewTypeImplementor<?>, String> elementInheritanceSubtypeMappings() {
        return (Map<ManagedViewTypeImplementor<?>, String>) (Map<?, ?>) inheritanceSubtypes;
    }

    protected Type<?> getKeyType() {
        return null;
    }

    protected Map<ManagedViewTypeImplementor<?>, String> keyInheritanceSubtypeMappings() {
        return null;
    }

    protected boolean isKeySubview() {
        return false;
    }

    @Override
    public boolean isSubview() {
        return type.getMappingType() != Type.MappingType.BASIC;
    }
}
