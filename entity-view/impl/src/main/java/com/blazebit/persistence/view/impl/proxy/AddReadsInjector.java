/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.proxy;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
public class AddReadsInjector {

    private AddReadsInjector() {
    }

    public static void addReadsModule(Class<?> clazz) {
        AddReadsInjector.class.getModule().addReads(clazz.getModule());
    }
}
