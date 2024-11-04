/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.quarkus.runtime;

/**
 * @author Moritz Becker
 * @since 1.6.0
 */
public class BlazePersistenceInstanceUtil {

    public static final String DEFAULT_BLAZE_PERSISTENCE_NAME = "<default>";

    private BlazePersistenceInstanceUtil() {
    }

    public static boolean isDefaultBlazePersistenceInstance(String blazePersistanceInstanceName) {
        return DEFAULT_BLAZE_PERSISTENCE_NAME.equals(blazePersistanceInstanceName);
    }
}
