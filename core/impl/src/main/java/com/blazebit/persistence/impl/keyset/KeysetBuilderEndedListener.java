/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.keyset;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface KeysetBuilderEndedListener {

    public void onBuilderEnded(KeysetBuilderImpl<?> builder);
}
