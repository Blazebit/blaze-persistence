/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

/**
 * A listener for getting a callback.
 *
 * @param <T> The view type
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface ViewTransitionListener<T> {

    /**
     * A callback that is invoked for a view.
     *
     * @param view The view for this lifecycle event
     * @param transition The lifecycle event transition
     */
    public void call(T view, ViewTransition transition);
}
