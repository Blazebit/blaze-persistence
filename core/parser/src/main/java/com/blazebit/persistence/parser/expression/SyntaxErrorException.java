/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class SyntaxErrorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SyntaxErrorException() {
    }

    public SyntaxErrorException(Throwable t) {
        super(t);
    }

    public SyntaxErrorException(String msg) {
        super(msg);
    }

    public SyntaxErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
