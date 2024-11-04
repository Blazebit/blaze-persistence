/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update;

import com.blazebit.persistence.view.spi.type.MutableStateTrackable;
import com.blazebit.persistence.view.impl.update.flush.DirtyAttributeFlusher;

import javax.persistence.Query;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface UpdateQueryFactory {

    public Query createUpdateQuery(UpdateContext context, MutableStateTrackable view, DirtyAttributeFlusher<?, ?, ?> nestedGraphNode);

}
