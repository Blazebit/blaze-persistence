/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface CTEBuilderListener {

    public void onReplaceBuilder(CTEInfoBuilder oldBuilder, CTEInfoBuilder newBuilder);

    public void onBuilderEnded(CTEInfoBuilder builder);

    public void onBuilderStarted(CTEInfoBuilder builder);
}
