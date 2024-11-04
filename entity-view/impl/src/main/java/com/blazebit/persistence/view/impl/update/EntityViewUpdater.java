/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update;

import com.blazebit.persistence.view.impl.change.DirtyChecker;
import com.blazebit.persistence.view.spi.type.DirtyStateTrackable;
import com.blazebit.persistence.view.spi.type.MutableStateTrackable;
import com.blazebit.persistence.view.impl.update.flush.DirtyAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.FetchGraphNode;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface EntityViewUpdater extends UpdateQueryFactory {

    public FetchGraphNode<?> getFullGraphNode();

    public DirtyAttributeFlusher<?, ?, ?> getIdFlusher();

    public <T extends DirtyAttributeFlusher<T, E, V>, E, V> DirtyAttributeFlusher<T, E, V> getNestedDirtyFlusher(UpdateContext context, MutableStateTrackable current, DirtyAttributeFlusher<T, E, V> fullFlusher);

    public boolean executeUpdate(UpdateContext context, MutableStateTrackable updatableProxy);

    public Object executeUpdate(UpdateContext context, Object entity, MutableStateTrackable updatableProxy);

    public Object executePersist(UpdateContext context, MutableStateTrackable updatableProxy);

    public Object executePersist(UpdateContext context, Object entity, MutableStateTrackable updatableProxy);

    public void remove(UpdateContext context, EntityViewProxy entityView);

    public void remove(UpdateContext context, Object id);

    public DirtyChecker<DirtyStateTrackable> getDirtyChecker();
}
