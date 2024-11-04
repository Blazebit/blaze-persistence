/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import java.util.Set;

/**
 * A contract that checks if the caller of our caller is trusted. On Java 9 this will actually check modules.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
class CallerChecker {

    private CallerChecker() {
    }

    public static boolean isCallerTrusted() {
        return StackWalker.getInstance(Set.of(), 2).walk(stackFrames -> {
            return stackFrames.limit(2).allMatch(s -> s.getClassName().startsWith("com.blazebit.persistence."));
        });
    }
}
