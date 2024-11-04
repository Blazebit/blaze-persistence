/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.visibility.model1;

import com.blazebit.persistence.view.IdMapping;

import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public abstract class IdHolderView<T> implements Serializable {

    public IdHolderView() {
        setId((T) (Long) Long.MIN_VALUE);
    }

    @IdMapping
    protected abstract T getId();

    abstract void setId(T id);
}