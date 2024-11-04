/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.fragments.basic.view;

import com.blazebit.persistence.view.IdMapping;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public interface IdHolderView<T> {

    @IdMapping
    T getId();

}
