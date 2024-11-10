/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.collection.RecordingEntrySetReplacingIterator;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.UpdateQueryFactory;

import jakarta.persistence.Query;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class MergeMapKeyAttributeFlusher<E, V> extends MergeCollectionElementAttributeFlusher<E, V> {

    public MergeMapKeyAttributeFlusher(Object element, boolean optimisticLockProtected) {
        super(element, optimisticLockProtected);
    }

    @Override
    public Query flushQuery(UpdateContext context, String parameterPrefix, UpdateQueryFactory queryFactory, Query query, Object ownerView, Object view, V value, UnmappedOwnerAwareDeleter ownerAwareDeleter, DirtyAttributeFlusher<?, ?, ?> ownerFlusher) {
        RecordingEntrySetReplacingIterator<Object, Object> recordingIterator = (RecordingEntrySetReplacingIterator<Object, Object>) ((RecordingMap<?, ?, ?>) value).recordingIterator();
        try {
            while (recordingIterator.hasNext()) {
                if (recordingIterator.next().getKey() == element) {
                    break;
                }
            }
            Object newObject = context.getEntityManager().merge(element);
            Object val = recordingIterator.replace();
            recordingIterator.add(newObject, val);

            while (recordingIterator.hasNext()) {
                recordingIterator.next();
            }
            return query;
        } finally {
            ((RecordingMap<?, ?, ?>) value).resetRecordingIterator();
        }
    }
}
