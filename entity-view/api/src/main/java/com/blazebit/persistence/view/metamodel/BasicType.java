/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.view.spi.type.BasicUserType;

/**
 * Represents the metamodel of a basic type.
 *
 * @param <X> The type of the java type represented by this basic type
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BasicType<X> extends Type<X> {

    /**
     * The user type implementation for this basic type.
     *
     * @return The user type
     */
    public BasicUserType<X> getUserType();
}
