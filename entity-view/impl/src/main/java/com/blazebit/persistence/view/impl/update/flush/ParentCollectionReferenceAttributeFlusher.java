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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.accessor.InitialValueAttributeAccessor;
import com.blazebit.persistence.view.impl.collection.CollectionRemoveListener;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import java.util.Collection;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class ParentCollectionReferenceAttributeFlusher<E, V extends Collection<?>> extends CollectionAttributeFlusher<E, V> {

    public ParentCollectionReferenceAttributeFlusher(String attributeName, String mapping, FlushStrategy flushStrategy, AttributeAccessor entityAttributeAccessor, InitialValueAttributeAccessor viewAttributeAccessor,
                                                     CollectionRemoveListener cascadeDeleteListener, CollectionRemoveListener removeListener, TypeDescriptor elementDescriptor) {
        super(attributeName, mapping, null, null, flushStrategy, entityAttributeAccessor, viewAttributeAccessor, false, true, false, false, cascadeDeleteListener, removeListener, null, elementDescriptor, null, null);
    }

    @Override
    public boolean supportsQueryFlush() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean flushEntity(UpdateContext context, E entity, Object view, V value, Runnable postReplaceListener) {
        Collection<Object> collection = (Collection<Object>) entityAttributeMapper.getValue(value);
        collection.add(entity);
        return true;
    }
}
