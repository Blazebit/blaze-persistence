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
import com.blazebit.persistence.view.metamodel.CollectionAttribute;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractMethodCollectionAttribute<X, Y> extends AbstractMethodPluralAttribute<X, Collection<Y>, Y> implements CollectionAttribute<X, Y> {

    public AbstractMethodCollectionAttribute(ManagedViewType<X> viewType, Method method, Annotation mapping, Set<Class<?>> entityViews, Set<String> errors) {
        super(viewType, method, mapping, entityViews, false, errors);
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
