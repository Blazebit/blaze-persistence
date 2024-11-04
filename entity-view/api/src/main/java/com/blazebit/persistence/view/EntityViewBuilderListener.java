/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

/**
 * A listener that is invoked after an entity view was built.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface EntityViewBuilderListener {

    /**
     * The callback that is called after an entity view is built.
     *
     * @param object The built entity view
     */
    public void onBuildComplete(Object object);

}
