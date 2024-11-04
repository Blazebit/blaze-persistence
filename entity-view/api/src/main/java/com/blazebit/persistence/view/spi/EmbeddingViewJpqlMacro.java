/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.spi;

import com.blazebit.persistence.spi.JpqlMacro;

/**
 * Interface implemented by the entity view provider.
 *
 * Represents a embedding view that gives access to the embedding view.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface EmbeddingViewJpqlMacro extends JpqlMacro {

    /**
     * Returns whether the macro was used so far.
     *
     * @return whether the macro was used so far
     */
    public boolean usesEmbeddingView();

    /**
     * Returns the current embedding view path.
     *
     * @return the current embedding view path
     */
    public String getEmbeddingViewPath();

    /**
     * Sets the current embedding view path.
     *
     * @param embeddingViewPath The new embedding view path
     */
    public void setEmbeddingViewPath(String embeddingViewPath);
}
