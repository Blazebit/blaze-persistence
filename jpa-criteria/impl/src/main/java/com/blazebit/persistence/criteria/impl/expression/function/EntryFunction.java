/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression.function;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.RenderContext.ClauseType;
import com.blazebit.persistence.criteria.impl.expression.AbstractExpression;
import com.blazebit.persistence.criteria.impl.path.AbstractPath;
import com.blazebit.persistence.criteria.impl.path.TreatedPath;

import javax.persistence.criteria.Expression;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntryFunction<K, V> extends AbstractExpression<Map.Entry<K, V>> implements Expression<Map.Entry<K, V>> {

    private static final long serialVersionUID = 1L;

    private final AbstractPath<?> origin;

    public EntryFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Class<Map.Entry<K, V>> javaType, AbstractPath<?> origin) {
        super(criteriaBuilder, javaType);
        if (origin instanceof TreatedPath<?>) {
            this.origin = ((TreatedPath) origin).getTreatedPath();
        } else {
            this.origin = origin;
        }
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        // no-op
    }

    @Override
    public void render(RenderContext context) {
        if (context.getClauseType() != ClauseType.SELECT) {
            throw new IllegalStateException("Entry is only allowed in select clause");
        }

        final StringBuilder buffer = context.getBuffer();
        buffer.append("ENTRY(");
        origin.renderPathExpression(context);
        buffer.append(')');
    }

}
