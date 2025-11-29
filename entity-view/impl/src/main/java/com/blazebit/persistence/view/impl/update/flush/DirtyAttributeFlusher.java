/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.change.DirtyChecker;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.UpdateQueryFactory;

import jakarta.persistence.Query;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface DirtyAttributeFlusher<T extends DirtyAttributeFlusher<T, E, V>, E, V> extends FetchGraphNode<T>, DirtyChecker<V> {

    public DirtyAttributeFlusher<T, E, V> getDirtyFlusher(UpdateContext context, Object view, Object initial, Object current);

    public boolean appendUpdateQueryFragment(UpdateContext context, StringBuilder sb, String mappingPrefix, String parameterPrefix, String separator);

    public void appendFetchJoinQueryFragment(String base, StringBuilder sb);

    public boolean supportsQueryFlush();

    public boolean loadForEntityFlush();

    public Object getNewInitialValue(UpdateContext context, V clonedValue, V currentValue);
    
    public Query flushQuery(UpdateContext context, String parameterPrefix, UpdateQueryFactory queryFactory, Query query, Object ownerView, Object view, V value, UnmappedOwnerAwareDeleter ownerAwareDeleter, DirtyAttributeFlusher<?, ?, ?> ownerFlusher);

    public boolean flushEntity(UpdateContext context, E entity, Object ownerView, Object view, V value, Runnable postReplaceListener);

    public void removeFromEntity(UpdateContext context, E entity);

    public List<PostFlushDeleter> remove(UpdateContext context, E entity, Object view, V value);

    public void remove(UpdateContext context, Object id);

    public List<PostFlushDeleter> removeByOwnerId(UpdateContext context, Object id);

    public V cloneDeep(Object view, V oldValue, V newValue);

    public boolean isPassThrough();

    public String getElementIdAttributeName();

    public AttributeAccessor getViewAttributeAccessor();

    public AttributeAccessor getEntityAttributeAccessor();

    public String getLockOwner();

    public boolean isOptimisticLockProtected();

    public boolean requiresFlushAfterPersist(V value);

    public boolean requiresDeferredFlush(V value);

    public boolean requiresDeleteCascadeAfterRemove();

    public boolean isViewOnlyDeleteCascaded();
}
