/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.transform;

import com.blazebit.persistence.impl.AbstractManager;
import com.blazebit.persistence.parser.expression.modifier.ExpressionModifier;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public interface ExpressionTransformerGroup<T extends ExpressionModifier> {

    void applyExpressionTransformer(AbstractManager<? extends T> manager);

    void beforeTransformationGroup();

    void afterTransformationGroup();

    void afterAllTransformations();

}
