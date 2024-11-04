/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.spi;

import com.blazebit.persistence.spi.JpqlMacro;

/**
 * Interface implemented by the entity view provider.
 *
 * Represents a view root macro that gives access to the view root.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ViewRootJpqlMacro extends JpqlMacro {

    /**
     * Returns the view root alias or <code>null</code> if not an alias.
     *
     * When using batched fetching, it can happen that a view root is represented as parameter. In that case <code>null</code> is returned.
     *
     * @return The view root or <code>null</code>
     */
    public String getViewRoot();

}
