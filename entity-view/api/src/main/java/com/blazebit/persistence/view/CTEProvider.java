/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import com.blazebit.persistence.CTEBuilder;

import java.util.Map;

/**
 * Provides CTE bindings to a {@link CTEBuilder}.
 *
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public interface CTEProvider {

    /**
     * Binds a CTE.
     *
     * @param builder the builder
     * @param optionalParameters The optional parameters of the entity view setting
     */
    void applyCtes(CTEBuilder<?> builder, Map<String, Object> optionalParameters);
}
