/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import java.io.Serializable;

import com.blazebit.persistence.view.IdMapping;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface IdHolderView<T extends Serializable> extends Serializable {

    @IdMapping
    public T getId();
}
