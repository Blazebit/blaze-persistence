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

package com.blazebit.persistence.view.impl.change;

import com.blazebit.persistence.view.impl.metamodel.BasicTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;

import java.util.Collection;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CollectionChangeModelImpl<V> extends AbstractCollectionChangeModel<Collection<V>, V> {

    public CollectionChangeModelImpl(ManagedViewTypeImplementor<V> type, BasicTypeImpl<V> basicType, Collection<V> initial, Collection<V> current, PluralDirtyChecker<Collection<V>, V> pluralDirtyChecker) {
        super(type, basicType, initial, current, pluralDirtyChecker);
    }
}
