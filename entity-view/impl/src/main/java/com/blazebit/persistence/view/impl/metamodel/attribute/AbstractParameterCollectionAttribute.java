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

package com.blazebit.persistence.view.impl.metamodel.attribute;

import com.blazebit.persistence.view.impl.collection.CollectionInstantiator;
import com.blazebit.persistence.view.impl.collection.MapInstantiator;
import com.blazebit.persistence.view.impl.metamodel.AbstractParameterPluralAttribute;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBuildingContext;
import com.blazebit.persistence.view.impl.metamodel.ParameterAttributeMapping;
import com.blazebit.persistence.view.metamodel.CollectionAttribute;
import com.blazebit.persistence.view.metamodel.Type;

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractParameterCollectionAttribute<X, Y> extends AbstractParameterPluralAttribute<X, Collection<Y>, Y> implements CollectionAttribute<X, Y> {

    private final CollectionInstantiator collectionInstantiator;

    public AbstractParameterCollectionAttribute(MappingConstructorImpl<X> mappingConstructor, ParameterAttributeMapping mapping, MetamodelBuildingContext context) {
        super(mappingConstructor, mapping, context);
        this.collectionInstantiator = createCollectionInstantiator(null, isIndexed(), isSorted(), isOrdered(), getComparator());
    }

    @Override
    public CollectionType getCollectionType() {
        return CollectionType.COLLECTION;
    }

    @Override
    public CollectionInstantiator getCollectionInstantiator() {
        return collectionInstantiator;
    }

    @Override
    public MapInstantiator getMapInstantiator() {
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
