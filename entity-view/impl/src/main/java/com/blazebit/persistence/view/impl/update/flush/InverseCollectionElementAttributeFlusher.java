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

import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import javax.persistence.Query;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class InverseCollectionElementAttributeFlusher<E, V> extends CollectionElementAttributeFlusher<E, V> {

    private final InverseFlusher<E> inverseFlusher;
    private final Strategy strategy;

    public InverseCollectionElementAttributeFlusher(DirtyAttributeFlusher<?, E, V> nestedGraphNode, Object element, boolean optimisticLockProtected, InverseFlusher<E> inverseFlusher, Strategy strategy) {
        super(nestedGraphNode, element, optimisticLockProtected);
        this.inverseFlusher = inverseFlusher;
        this.strategy = strategy;
    }

    @Override
    public void flushQuery(UpdateContext context, String parameterPrefix, Query query, Object view, V value, UnmappedOwnerAwareDeleter ownerAwareDeleter) {
        if (strategy == Strategy.REMOVE) {
            inverseFlusher.removeElement(context, null, element);
        } else if (strategy != Strategy.IGNORE) {
            inverseFlusher.flushQuerySetElement(context, (V)  element, strategy == Strategy.SET_NULL ? null : view, parameterPrefix, (DirtyAttributeFlusher<?, E, Object>) nestedGraphNode);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean flushEntity(UpdateContext context, E entity, Object view, V value, Runnable postReplaceListener) {
        if (strategy == Strategy.REMOVE) {
            inverseFlusher.removeElement(context, entity, element);
        } else if (strategy != Strategy.IGNORE) {
            inverseFlusher.flushEntitySetElement(context, (V) element, strategy == Strategy.SET_NULL ? null : entity, (DirtyAttributeFlusher<?, E, Object>) nestedGraphNode);
        }
        return true;
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static enum Strategy {
        SET,
        SET_NULL,
        REMOVE,
        IGNORE;

        public static Strategy of(InverseRemoveStrategy inverseRemoveStrategy) {
            if (inverseRemoveStrategy == null) {
                return null;
            }
            switch (inverseRemoveStrategy) {
                case SET_NULL: return SET_NULL;
                case REMOVE: return REMOVE;
                case IGNORE: return IGNORE;
                default: break;
            }
            throw new IllegalArgumentException("Unsupported remove strategy: " + inverseRemoveStrategy);
        }
    }

}
