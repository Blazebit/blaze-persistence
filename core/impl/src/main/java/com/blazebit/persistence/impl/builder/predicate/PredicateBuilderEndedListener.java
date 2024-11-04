/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.builder.predicate;

import com.blazebit.persistence.parser.predicate.PredicateBuilder;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public interface PredicateBuilderEndedListener {

    public void onBuilderEnded(PredicateBuilder o);
}
