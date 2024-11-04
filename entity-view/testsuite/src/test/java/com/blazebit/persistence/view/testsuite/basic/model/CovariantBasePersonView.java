/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface CovariantBasePersonView<T extends CharSequence> extends IdHolderView<Long> {

    public T getName();
}
