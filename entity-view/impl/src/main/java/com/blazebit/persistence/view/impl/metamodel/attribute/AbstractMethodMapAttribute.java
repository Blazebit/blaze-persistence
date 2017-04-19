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
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.Type;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractMethodMapAttribute<X, K, V> extends AbstractMethodPluralAttribute<X, Map<K, V>, V> implements MapAttribute<X, K, V> {

    private final Type<K> keyType;
    private final Map<ManagedViewType<? extends K>, String> keyInheritanceSubtypes;

    @SuppressWarnings("unchecked")
    public AbstractMethodMapAttribute(ManagedViewTypeImpl<X> viewType, MethodAttributeMapping mapping, MetamodelBuildingContext context) {
        super(viewType, mapping, context);
        this.keyType = (Type<K>) mapping.getKeyType();
        this.keyInheritanceSubtypes = (Map<ManagedViewType<? extends K>, String>) (Map<?, ?>) mapping.getKeyInheritanceSubtypes();
        if (isIgnoreIndex()) {
            context.addError("Illegal ignoreIndex mapping for the " + mapping.getErrorLocation());
        }
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
    protected Map<ManagedViewTypeImpl<?>, String> keyInheritanceSubtypeMappings() {
        return (Map<ManagedViewTypeImpl<?>, String>) (Map<?, ?>) keyInheritanceSubtypes;
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
    public boolean isIndexed() {
        return true;
    }

}
