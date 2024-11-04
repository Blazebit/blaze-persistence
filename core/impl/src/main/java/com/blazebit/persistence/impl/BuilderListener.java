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
public interface BuilderListener<T> {

    public void onReplaceBuilder(T oldBuilder, T newBuilder);

    public void onBuilderEnded(T builder);

    public void onBuilderStarted(T builder);
    
    public boolean isBuilderEnded();
}
