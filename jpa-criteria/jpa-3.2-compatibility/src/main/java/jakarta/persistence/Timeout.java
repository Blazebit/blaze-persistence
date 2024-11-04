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
 * Specifies a timeout for a database request. This option is
 * always a hint, and may be ignored by the provider.
 *
 * @since 3.2
 */
public class Timeout implements FindOption, RefreshOption, LockOption {
    private final int milliseconds;

    private Timeout(int milliseconds) {
        this.milliseconds = milliseconds;
    }

    /**
     * Specify a timeout in seconds.
     * (Abbreviation of {@link #seconds(int)}.)
     */
    public static Timeout s(int seconds) {
        return new Timeout(seconds*1_000);
    }

    /**
     * Specify a timeout in milliseconds.
     * (Abbreviation of {@link #milliseconds(int)}.)
     */
    public static Timeout ms(int milliseconds) {
        return new Timeout(milliseconds);
    }

    /**
     * Specify a timeout in seconds.
     */
    public static Timeout seconds(int seconds) {
        return new Timeout(seconds*1_000);
    }

    /**
     * Specify a timeout in milliseconds.
     */
    public static Timeout milliseconds(int milliseconds) {
        return new Timeout(milliseconds);
    }

    /**
     * The timeout in milliseconds.
     */
    public int milliseconds() {
        return milliseconds;
    }
}
