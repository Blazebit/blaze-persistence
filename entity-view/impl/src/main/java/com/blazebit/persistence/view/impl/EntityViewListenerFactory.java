/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface EntityViewListenerFactory<T> {

    Class<? super T> getListenerKind();

    Class<T> getListenerClass();

    T createListener();

}
