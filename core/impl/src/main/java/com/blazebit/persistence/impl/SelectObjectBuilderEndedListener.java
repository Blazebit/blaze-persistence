/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import java.util.Collection;
import java.util.Map;

import com.blazebit.persistence.parser.expression.Expression;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public interface SelectObjectBuilderEndedListener {

    /**
     * 
     * @param expressions Collection containing map entries with expressions and their select aliases.
     *            A select alias is null if none was specified.
     */
    public void onBuilderEnded(Collection<Map.Entry<Expression, String>> expressions);

}
