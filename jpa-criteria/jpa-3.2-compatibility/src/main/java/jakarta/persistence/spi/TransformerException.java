/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

package jakarta.persistence.spi;

/**
 * Thrown by the persistence provider when a problem during
 * class re-definition occurs.
 *
 * @since 3.1
 */
public class TransformerException extends Exception {

    private static final long serialVersionUID = 7484555485977030491L;

    /**
     * Constructs a new {@code TransformerException} exception
     * with {@code null} as its detail message.
     */
    public TransformerException() {
        super();
    }

    /**
     * Constructs a new {@code TransformerException} exception
     * with the specified detail message.
     *
     * @param message the detail message.
     */
    public TransformerException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code TransformerException} exception
     * with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public TransformerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code TransformerException} exception
     * with the specified cause.
     *
     * @param cause the cause.
     */
    public TransformerException(Throwable cause) {
        super(cause);
    }
}
