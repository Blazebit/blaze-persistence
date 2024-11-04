/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

import java.util.List;

/**
 * Interface for implementing processing of values produced by a JPQL function in the SELECT clause.
 *
 * @param <T> The type this processor handles
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface JpqlFunctionProcessor<T> {

    /**
     * Processes the result set object.
     * 
     * @param result The result set object
     * @param arguments The JPQL function arguments
     * @return Returns the processed result set object
     */
    public Object process(T result, List<Object> arguments);

}
