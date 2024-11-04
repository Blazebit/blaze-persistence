/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.simple.model;

import com.blazebit.persistence.view.IdMapping;

import java.io.Serializable;

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
