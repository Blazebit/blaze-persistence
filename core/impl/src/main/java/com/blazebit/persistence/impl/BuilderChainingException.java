/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

/**
 *
 * @author Moritz Becker
 * @since 1.0.5
 */
public class BuilderChainingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of <code>BuilderNotEndedException</code> without
     * detail message.
     */
    public BuilderChainingException() {
    }

    /**
     * Constructs an instance of <code>BuilderNotEndedException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public BuilderChainingException(String msg) {
        super(msg);
    }
}
