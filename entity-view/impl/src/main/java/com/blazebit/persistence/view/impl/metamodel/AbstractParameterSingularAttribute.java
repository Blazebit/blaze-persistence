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

import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.Type;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractParameterSingularAttribute<X, Y> extends AbstractParameterAttribute<X, Y> implements SingularAttribute<X, Y> {

    private final Type<Y> type;

    @SuppressWarnings("unchecked")
    public AbstractParameterSingularAttribute(MappingConstructorImpl<X> constructor, ParameterAttributeMapping mapping, MetamodelBuildingContext context) {
        super(constructor, mapping, context);
        this.type = (Type<Y>) mapping.getType();
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public boolean isIndexed() {
        return false;
    }

    @Override
    protected PluralAttribute.CollectionType getCollectionType() {
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
    protected Type<?> getElementType() {
        return type;
    }

    @Override
    public boolean isSubview() {
        return type.getMappingType() != Type.MappingType.BASIC;
    }
}
