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

import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.change.DirtyChecker;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import javax.persistence.Query;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class CollectionElementAttributeFlusher<E, V> extends CollectionElementFetchGraphNode<CollectionElementAttributeFlusher<E, V>, DirtyAttributeFlusher<?, E, V>> implements DirtyAttributeFlusher<CollectionElementAttributeFlusher<E, V>, E, V> {

    protected final Object element;

    private final boolean optimisticLockProtected;

    public CollectionElementAttributeFlusher(DirtyAttributeFlusher<?, E, V> nestedGraphNode, Object element, boolean optimisticLockProtected) {
        super(nestedGraphNode);
        this.element = element;
        this.optimisticLockProtected = optimisticLockProtected;
    }

    public Object getElement() {
        return element;
    }

    @Override
    public V cloneDeep(Object view, V oldValue, V newValue) {
        return newValue;
    }

    @Override
    public Object getNewInitialValue(UpdateContext context, V clonedValue, V currentValue) {
        return currentValue;
    }

    @Override
    public void appendUpdateQueryFragment(UpdateContext context, StringBuilder sb, String mappingPrefix, String parameterPrefix, String separator) {
        nestedGraphNode.appendUpdateQueryFragment(context, sb, mappingPrefix, parameterPrefix, separator);
    }

    @Override
    public boolean supportsQueryFlush() {
        return nestedGraphNode.supportsQueryFlush();
    }

    @Override
    public boolean loadForEntityFlush() {
        return nestedGraphNode.loadForEntityFlush();
    }

    @Override
    public void flushQuery(UpdateContext context, String parameterPrefix, Query query, Object view, V value, UnmappedOwnerAwareDeleter ownerAwareDeleter) {
        nestedGraphNode.flushQuery(context, parameterPrefix, null, null, (V) element, ownerAwareDeleter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean flushEntity(UpdateContext context, E entity, Object view, V value, Runnable postReplaceListener) {
        return nestedGraphNode.flushEntity(context, null, null, (V) element, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PostFlushDeleter> remove(UpdateContext context, E entity, Object view, V value) {
        // No-op
        return Collections.emptyList();
    }

    @Override
    public void remove(UpdateContext context, Object id) {
        // No-op
    }

    @Override
    public List<PostFlushDeleter> removeByOwnerId(UpdateContext context, Object id) {
        // No-op
        return Collections.emptyList();
    }

    @Override
    public void removeFromEntity(UpdateContext context, E entity) {
    }

    @Override
    public String getElementIdAttributeName() {
        return null;
    }

    @Override
    public boolean requiresDeleteCascadeAfterRemove() {
        return false;
    }

    @Override
    public boolean isViewOnlyDeleteCascaded() {
        return false;
    }

    @Override
    public boolean isPassThrough() {
        return false;
    }

    @Override
    public AttributeAccessor getViewAttributeAccessor() {
        return null;
    }

    @Override
    public boolean isOptimisticLockProtected() {
        return optimisticLockProtected;
    }

    @Override
    public boolean requiresFlushAfterPersist(V value) {
        return false;
    }

    @Override
    public <X> DirtyChecker<X>[] getNestedCheckers(V current) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DirtyKind getDirtyKind(V initial, V current) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DirtyAttributeFlusher<CollectionElementAttributeFlusher<E, V>, E, V> getDirtyFlusher(UpdateContext context, Object view, Object initial, Object current) {
        // Actually this should never be called, but let's return this to be safe
        return this;
    }
}
