/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class ExternalAliasDereferencingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExternalAliasDereferencingException() {
    }

    public ExternalAliasDereferencingException(String msg) {
        super(msg);
    }

    public ExternalAliasDereferencingException(Throwable t) {
        super(t);
    }

    public ExternalAliasDereferencingException(String msg, Throwable t) {
        super(msg, t);
    }
}
