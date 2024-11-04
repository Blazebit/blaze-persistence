/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.collection.RecordingEntrySetReplacingIterator;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.UpdateQueryFactory;

import javax.persistence.Query;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class MergeMapValueAttributeFlusher<E, V> extends MergeCollectionElementAttributeFlusher<E, V> {

    public MergeMapValueAttributeFlusher(Object element, boolean optimisticLockProtected) {
        super(element, optimisticLockProtected);
    }

    @Override
    public Query flushQuery(UpdateContext context, String parameterPrefix, UpdateQueryFactory queryFactory, Query query, Object ownerView, Object view, V value, UnmappedOwnerAwareDeleter ownerAwareDeleter, DirtyAttributeFlusher<?, ?, ?> ownerFlusher) {
        RecordingEntrySetReplacingIterator<Object, Object> recordingIterator = (RecordingEntrySetReplacingIterator<Object, Object>) ((RecordingMap<?, ?, ?>) value).recordingIterator();
        try {
            Object key = null;
            while (recordingIterator.hasNext()) {
                Map.Entry<Object, Object> entry = recordingIterator.next();
                if (entry.getValue() == element) {
                    key = entry.getKey();
                    break;
                }
            }
            Object newObject = context.getEntityManager().merge(element);
            recordingIterator.replace();
            recordingIterator.add(key, newObject);

            while (recordingIterator.hasNext()) {
                recordingIterator.next();
            }
            return query;
        } finally {
            ((RecordingMap<?, ?, ?>) value).resetRecordingIterator();
        }
    }
}
