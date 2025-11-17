/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Gavin King      - 3.2

package jakarta.persistence;

/**
 * Thrown when {@link SchemaManager#validate() schema validation} fails.
 *
 * @see SchemaManager#validate()
 *
 * @since 3.2
 */
public class SchemaValidationException extends Exception {
    private final Exception[] failures;

    /**
     * An array of problems detected while validating the schema.
     *
     * <p>A persistence provider might choose to fail fast upon
     * encountering a problem with one database object, in which
     * case there is only one problem reported here. Alternatively,
     * a provider might choose to continue validating the remaining
     * database objects, in which case multiple problems might be
     * reported, each as a separate exception instance.
     */
    public Exception[] getFailures() {
        return failures;
    }

    /**
     * Constructs a new instance with a message and, optionally,
     * an array of exceptions, each representing a problem detected
     * while validating the schema.
     * @param message an overall message
     * @param failures an array of exceptions, each representing a
     *                 separate problem
     */
    public SchemaValidationException(String message, Exception... failures) {
        super(message);
        this.failures = failures == null ? new Exception[0] : failures;
    }

}
