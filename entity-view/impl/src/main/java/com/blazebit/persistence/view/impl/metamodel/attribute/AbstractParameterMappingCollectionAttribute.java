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

import com.blazebit.persistence.view.impl.metamodel.AbstractParameterMappingPluralAttribute;
import com.blazebit.persistence.view.metamodel.CollectionAttribute;
import com.blazebit.persistence.view.metamodel.MappingConstructor;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractParameterMappingCollectionAttribute<X, Y> extends AbstractParameterMappingPluralAttribute<X, Collection<Y>, Y> implements CollectionAttribute<X, Y> {

    public AbstractParameterMappingCollectionAttribute(MappingConstructor<X> mappingConstructor, int index, Annotation mapping, Set<Class<?>> entityViews, Set<String> errors) {
        super(mappingConstructor, index, mapping, entityViews, false, errors);
    }

    @Override
    public CollectionType getCollectionType() {
        return CollectionType.COLLECTION;
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

}
