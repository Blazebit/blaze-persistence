/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.spi;

import com.blazebit.persistence.spi.JpqlMacro;

/**
 * Interface implemented by the entity view provider.
 *
 * Represents the current view that is accessible through the expression <code>VIEW()</code>.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface ViewJpqlMacro extends JpqlMacro {

    /**
     * Returns the current view path.
     *
     * @return the current view path
     */
    public String getViewPath();

    /**
     * Sets the current view path.
     *
     * @param viewPath The new view path
     */
    public void setViewPath(String viewPath);
}
