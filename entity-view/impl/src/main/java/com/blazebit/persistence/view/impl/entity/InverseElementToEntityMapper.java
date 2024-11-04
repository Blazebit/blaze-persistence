/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.entity;

import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.flush.DirtyAttributeFlusher;

import javax.persistence.Query;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface InverseElementToEntityMapper<E> {

    public void flushEntity(UpdateContext context, Object oldParent, Object newParent, Object child, DirtyAttributeFlusher<?, E, Object> nestedGraphNode);

    public Query createInverseUpdateQuery(UpdateContext context, Object element, DirtyAttributeFlusher<?, E, Object> nestedGraphNode, DirtyAttributeFlusher<?, ?, ?> inverseAttributeFlusher);

}
