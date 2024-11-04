/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;

import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.Selection;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CompoundSelectionImpl<X> extends AbstractSelection<X> implements CompoundSelection<X> {

    private static final long serialVersionUID = 1L;

    private final List<Selection<?>> selectionItems;

    public CompoundSelectionImpl(BlazeCriteriaBuilderImpl criteriaBuilder, Class<X> javaType, List<Selection<?>> selectionItems) {
        super(criteriaBuilder, javaType);
        this.selectionItems = selectionItems;
    }

    @Override
    public boolean isCompoundSelection() {
        return true;
    }

    @Override
    public List<Selection<?>> getCompoundSelectionItems() {
        return selectionItems;
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        for (Selection<?> selectionItem : selectionItems) {
            visitor.visit(selectionItem);
        }
    }

    @Override
    public void render(RenderContext context) {
        throw new AssertionError("Compound selection rendering should happen in InternalQuery!");
    }

}
