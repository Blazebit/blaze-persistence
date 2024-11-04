/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.path.AbstractFrom;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class FromSelection<X> extends AbstractSelection<X> {

    private final AbstractFrom<?, X> from;

    public FromSelection(BlazeCriteriaBuilderImpl criteriaBuilder, AbstractFrom<?, X> from, String alias) {
        super(criteriaBuilder, from.getJavaType());
        this.from = from;
        this.setAlias(alias);
    }

    @Override
    public void render(RenderContext context) {
        from.render(context);
    }
}
