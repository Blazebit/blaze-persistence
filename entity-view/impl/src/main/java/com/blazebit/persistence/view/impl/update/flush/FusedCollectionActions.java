/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.update.UpdateContext;

import java.util.Collection;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public interface FusedCollectionActions {

    public int operationCount();

    public int getRemoveCount();

    public int getAddCount();

    public int getUpdateCount();

    public Collection<Object> getRemoved();

    public Collection<Object> getRemoved(UpdateContext context);

    public Collection<Object> getAdded();

    public Collection<Object> getAdded(UpdateContext context);
}
