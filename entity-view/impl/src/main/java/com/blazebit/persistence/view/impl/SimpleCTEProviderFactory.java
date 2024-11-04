/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.view.CTEProvider;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public class SimpleCTEProviderFactory implements CTEProviderFactory {

    private final Class<? extends CTEProvider> clazz;

    public SimpleCTEProviderFactory(Class<? extends CTEProvider> clazz) {
        this.clazz = clazz;
    }

    @Override
    public CTEProvider create() {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not instantiate the CTE provider: " + clazz.getName(), ex);
        }
    }

}
