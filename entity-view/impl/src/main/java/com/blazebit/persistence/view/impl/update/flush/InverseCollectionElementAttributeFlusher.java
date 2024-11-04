/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.UpdateQueryFactory;

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
    public Query flushQuery(UpdateContext context, String parameterPrefix, UpdateQueryFactory queryFactory, Query query, Object ownerView, Object view, V value, UnmappedOwnerAwareDeleter ownerAwareDeleter, DirtyAttributeFlusher<?, ?, ?> ownerFlusher) {
        if (strategy == Strategy.REMOVE) {
            inverseFlusher.removeElement(context, null, element);
        } else if (strategy != Strategy.IGNORE) {
            inverseFlusher.flushQuerySetElement(context, (V)  element, ownerView, strategy == Strategy.SET_NULL ? null : ownerView, parameterPrefix, (DirtyAttributeFlusher<?, E, Object>) nestedGraphNode);
        }
        return query;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean flushEntity(UpdateContext context, E entity, Object ownerView, Object view, V value, Runnable postReplaceListener) {
        if (strategy == Strategy.REMOVE) {
            inverseFlusher.removeElement(context, entity, element);
        } else if (strategy != Strategy.IGNORE) {
            inverseFlusher.flushEntitySetElement(context, (V) element, entity, strategy == Strategy.SET_NULL ? null : entity, (DirtyAttributeFlusher<?, E, Object>) nestedGraphNode);
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
