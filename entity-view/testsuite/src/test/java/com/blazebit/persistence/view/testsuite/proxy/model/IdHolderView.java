/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.proxy.model;

import java.io.Serializable;

import com.blazebit.persistence.view.IdMapping;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface IdHolderView<T> extends Serializable {

    @IdMapping
    public T getId();

    public void setId(T id);
}
