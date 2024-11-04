/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.view.CTEProvider;

/**
 *
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public interface CTEProviderFactory {

    public CTEProvider create();
}
