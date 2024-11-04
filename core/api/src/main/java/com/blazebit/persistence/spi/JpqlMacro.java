/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

/**
 * Interface for implementing a macro function that produces JPQL from it's parameters.
 * 
 * An instance of this interface needs to be registered to be able to use the macro in queries.
 * Consider implementing {@link CacheableJpqlMacro} if possible to allow expressions containing the macro to be cached.
 *
 * @author Christian Beikov
 * @since 1.2.0
 * @see CacheableJpqlMacro
 */
public interface JpqlMacro {

    /**
     * Renders the function into the given function render context.
     * 
     * @param context The context into which the function should be rendered
     */
    public void render(FunctionRenderContext context);

}
