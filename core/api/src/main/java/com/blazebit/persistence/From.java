/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

import jakarta.persistence.metamodel.Type;

/**
 * CAREFUL, this is an experimental API and will change!
 *
 * Represents a from node of a criteria builder.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface From {

    /**
     * The alias of the from node.
     *
     * @return The alias
     */
    public String getAlias();

    /**
     * The type of the from node.
     *
     * @return The type
     * @since 1.2.1
     */
    public Type<?> getType();

    /**
     * The type of the from node.
     *
     * @return The type
     */
    public Class<?> getJavaType();

    // TODO: add access to join nodes

}
