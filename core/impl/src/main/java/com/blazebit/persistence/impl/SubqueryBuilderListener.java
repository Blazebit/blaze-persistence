/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.SubqueryInitiator;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public interface SubqueryBuilderListener<T> {

    public void onReplaceBuilder(SubqueryInternalBuilder<T> oldBuilder, SubqueryInternalBuilder<T> newBuilder);

    public void onBuilderEnded(SubqueryInternalBuilder<T> builder);

    public void onBuilderStarted(SubqueryInternalBuilder<T> builder);

    public void onInitiatorStarted(SubqueryInitiator<?> initiator);
}
