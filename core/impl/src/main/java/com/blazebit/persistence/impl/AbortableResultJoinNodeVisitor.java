/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

/**
 *
 * @author Christian Beikov
 * @since 1.0.5
 */
public interface AbortableResultJoinNodeVisitor<T> extends ResultJoinNodeVisitor<T> {

    public T getStopValue();
}
