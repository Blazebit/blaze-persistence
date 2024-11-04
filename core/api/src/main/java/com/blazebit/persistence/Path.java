/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

import jakarta.persistence.metamodel.Type;

/**
 * CAREFUL, this is an experimental API and will change!
 *
 * A resolved path expression.
 *
 * @author Christian Beikov
 * @since 1.2.1
 */
public interface Path {

    /**
     * The from node on which this path is based.
     *
     * @return The from node
     */
    public From getFrom();

    /**
     * The qualified path as string.
     *
     * @return The qualified path
     */
    public String getPath();

    /**
     * The type of the path.
     *
     * @return The type
     */
    public Type<?> getType();

    /**
     * The java type of the path.
     *
     * @return The type
     */
    public Class<?> getJavaType();

}
