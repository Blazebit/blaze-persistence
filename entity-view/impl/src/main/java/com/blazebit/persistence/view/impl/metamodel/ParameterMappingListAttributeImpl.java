/*
 * Copyright 2014 Blazebit.
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

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.view.metamodel.ListAttribute;
import com.blazebit.persistence.view.metamodel.MappingConstructor;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class ParameterMappingListAttributeImpl<X, Y> extends AbstractParameterMappingPluralAttribute<X, List<Y>, Y> implements ListAttribute<X, Y> {

    private final boolean isIndexed;
    
    public ParameterMappingListAttributeImpl(MappingConstructor<X> mappingConstructor, int index, Annotation mapping, Set<Class<?>> entityViews, EntityMetamodel metamodel, ExpressionFactory expressionFactory) {
        super(mappingConstructor, index, mapping, entityViews, false);
        
        if (isIgnoreIndex()) {
        	this.isIndexed = false;
        } else {
        	this.isIndexed = MetamodelUtils.isIndexedList(metamodel, expressionFactory, mappingConstructor.getDeclaringType().getEntityClass(), mapping);
        }
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

}
