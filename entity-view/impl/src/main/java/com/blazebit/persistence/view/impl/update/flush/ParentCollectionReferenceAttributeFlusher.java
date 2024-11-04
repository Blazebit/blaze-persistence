/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
        super(attributeName, mapping, null, null, null, null, null, false, flushStrategy, entityAttributeAccessor, viewAttributeAccessor, false, true, false, false, cascadeDeleteListener, removeListener, null, elementDescriptor, null, null);
    }

    @Override
    public boolean supportsQueryFlush() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean flushEntity(UpdateContext context, E entity, Object ownerView, Object view, V value, Runnable postReplaceListener) {
        Collection<Object> collection = (Collection<Object>) entityAttributeAccessor.getValue(value);
        collection.add(entity);
        return true;
    }
}
