/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

/**
 * A listener that sets the built entity view on the given builder for the given attribute name.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class SingularNameEntityViewBuilderListener implements EntityViewBuilderListener {

    private final EntityViewBuilderBase<?, ?> builder;
    private final String attributeName;

    /**
     * Creates a new listener.
     *
     * @param builder The builder to set the built entity view on
     * @param attributeName The attribute name to set
     */
    public SingularNameEntityViewBuilderListener(EntityViewBuilderBase<?, ?> builder, String attributeName) {
        this.builder = builder;
        this.attributeName = attributeName;
    }

    @Override
    public void onBuildComplete(Object object) {
        builder.with(attributeName, object);
    }
}
