/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.batchsubselect.model;

import com.blazebit.persistence.view.IdMapping;

import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.6.9
 */
public interface IdHolderView<T> extends Serializable {

    @IdMapping
    public T getId();
}
