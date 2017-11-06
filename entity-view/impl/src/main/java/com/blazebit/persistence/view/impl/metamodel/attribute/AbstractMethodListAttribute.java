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

package com.blazebit.persistence.view.impl.metamodel.attribute;

import com.blazebit.persistence.view.impl.metamodel.AbstractMethodPluralAttribute;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBuildingContext;
import com.blazebit.persistence.view.impl.metamodel.MethodAttributeMapping;
import com.blazebit.persistence.view.metamodel.ListAttribute;
import com.blazebit.persistence.view.metamodel.Type;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractMethodListAttribute<X, Y> extends AbstractMethodPluralAttribute<X, List<Y>, Y> implements ListAttribute<X, Y> {

    private final boolean isIndexed;
    
    public AbstractMethodListAttribute(ManagedViewTypeImpl<X> viewType, MethodAttributeMapping mapping, MetamodelBuildingContext context, int dirtyStateIndex) {
        super(viewType, mapping, context, dirtyStateIndex);
        this.isIndexed = mapping.determineIndexed(context, context.getEntityMetamodel().getManagedType(viewType.getEntityClass()));
    }

    @Override
    public CollectionType getCollectionType() {
        return CollectionType.LIST;
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
    protected Type<?> getKeyType() {
        return null;
    }

    @Override
    protected Map<ManagedViewTypeImpl<?>, String> keyInheritanceSubtypeMappings() {
        return null;
    }

    @Override
    protected boolean isKeySubview() {
        return false;
    }

}
