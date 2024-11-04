/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.collection.RecordingReplacingIterator;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.UpdateQueryFactory;

import javax.persistence.Query;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MergeCollectionElementAttributeFlusher<E, V> extends CollectionElementAttributeFlusher<E, V> {

    public MergeCollectionElementAttributeFlusher(Object element, boolean optimisticLockProtected) {
        super(null, element, optimisticLockProtected);
    }

    @Override
    public FetchGraphNode<?> mergeWith(List<CollectionElementAttributeFlusher<E, V>> fetchGraphNodes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void appendFetchJoinQueryFragment(String base, StringBuilder sb) {
    }

    @Override
    public boolean appendUpdateQueryFragment(UpdateContext context, StringBuilder sb, String mappingPrefix, String parameterPrefix, String separator) {
        return false;
    }

    @Override
    public Query flushQuery(UpdateContext context, String parameterPrefix, UpdateQueryFactory queryFactory, Query query, Object ownerView, Object view, V value, UnmappedOwnerAwareDeleter ownerAwareDeleter, DirtyAttributeFlusher<?, ?, ?> ownerFlusher) {
        RecordingReplacingIterator<Object> recordingIterator = (RecordingReplacingIterator<Object>) ((RecordingCollection<?, ?>) value).recordingIterator();
        try {
            while (recordingIterator.hasNext()) {
                if (recordingIterator.next() == element) {
                    break;
                }
            }
            Object newObject = context.getEntityManager().merge(element);
            recordingIterator.replace();
            recordingIterator.add(newObject);

            while (recordingIterator.hasNext()) {
                recordingIterator.next();
            }
            return query;
        } finally {
            ((RecordingCollection<?, ?>) value).resetRecordingIterator();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean flushEntity(UpdateContext context, E entity, Object ownerView, Object view, V value, Runnable postReplaceListener) {
        flushQuery(context, null, null, null, null, view, value, null, null);
        return true;
    }

    @Override
    public DirtyAttributeFlusher<CollectionElementAttributeFlusher<E, V>, E, V> getDirtyFlusher(UpdateContext context, Object view, Object initial, Object current) {
        // Actually this should never be called, but let's return this to be safe
        return this;
    }
}
