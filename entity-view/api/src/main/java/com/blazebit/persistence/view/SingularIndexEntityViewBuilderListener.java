/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

/**
 * A listener that sets the built entity view on the given builder for the given parameter index.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class SingularIndexEntityViewBuilderListener implements EntityViewBuilderListener {

    private final EntityViewBuilderBase<?, ?> builder;
    private final int parameterIndex;

    /**
     * Creates a new listener.
     *
     * @param builder The builder to set the built entity view on
     * @param parameterIndex The parameter index to set
     */
    public SingularIndexEntityViewBuilderListener(EntityViewBuilderBase<?, ?> builder, int parameterIndex) {
        this.builder = builder;
        this.parameterIndex = parameterIndex;
    }

    @Override
    public void onBuildComplete(Object object) {
        builder.with(parameterIndex, object);
    }
}
