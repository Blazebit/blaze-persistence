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
 * @since 1.2.0
 */
public class SimpleTransformerGroup implements ExpressionTransformerGroup<ExpressionModifier> {

    private final ExpressionModifierVisitor<ExpressionModifier> visitor;

    public SimpleTransformerGroup(ExpressionModifierVisitor<ExpressionModifier> visitor) {
        this.visitor = visitor;
    }

    @Override
    public void applyExpressionTransformer(AbstractManager<? extends ExpressionModifier> manager) {
        manager.apply(visitor);
    }

    @Override
    public void beforeTransformationGroup() {
    }

    @Override
    public void afterTransformationGroup() {
    }

    @Override
    public void afterAllTransformations() {
    }
}
