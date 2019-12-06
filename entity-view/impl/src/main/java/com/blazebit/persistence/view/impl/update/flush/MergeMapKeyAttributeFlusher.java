/*
 * Copyright 2014 - 2019 Blazebit.
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

import com.blazebit.persistence.view.impl.collection.RecordingEntrySetReplacingIterator;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.UpdateQueryFactory;

import javax.persistence.Query;

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
    public Query flushQuery(UpdateContext context, String parameterPrefix, UpdateQueryFactory queryFactory, Query query, Object ownerView, Object view, V value, UnmappedOwnerAwareDeleter ownerAwareDeleter) {
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
