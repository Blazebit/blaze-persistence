/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.predicate;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public interface Negatable {

    public boolean isNegated();

    public void setNegated(boolean negated);

    public void negate();
}
