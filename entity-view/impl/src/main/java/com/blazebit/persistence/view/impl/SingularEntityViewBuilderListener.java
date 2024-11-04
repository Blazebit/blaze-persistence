/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.view.EntityViewBuilderListener;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class SingularEntityViewBuilderListener implements EntityViewBuilderListener {

    private final Object[] tuple;
    private final int index;

    public SingularEntityViewBuilderListener(Object[] tuple, int index) {
        this.tuple = tuple;
        this.index = index;
    }

    @Override
    public void onBuildComplete(Object object) {
        tuple[index] = object;
    }
}
