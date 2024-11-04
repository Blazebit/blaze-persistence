/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

/**
 * Marker interface to signal, that expressions using the macro may be cached.
 * 
 * This should be implemented, if the macro is stateless or the equals/hashCode implementations are solely based on it's state.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface CacheableJpqlMacro extends JpqlMacro {

}
