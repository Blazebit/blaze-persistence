/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicDirtyTracker;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface LobImplementor<T> extends BasicDirtyTracker {

    public T getWrapped();
}
