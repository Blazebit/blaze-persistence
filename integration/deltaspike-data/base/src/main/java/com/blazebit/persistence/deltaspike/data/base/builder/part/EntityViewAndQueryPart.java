/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.base.builder.part;

import com.blazebit.persistence.deltaspike.data.Specification;
import com.blazebit.persistence.deltaspike.data.base.builder.EntityViewQueryBuilderContext;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.part.AndQueryPart} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityViewAndQueryPart extends EntityViewConnectingQueryPart {

    public EntityViewAndQueryPart(boolean first) {
        super(first);
    }

    @Override
    public EntityViewQueryPart build(String queryPart, String method, Class<?> repositoryClass, Class<?> entityClass) {
        children.add(new EntityViewPropertyQueryPart().build(queryPart, method, repositoryClass, entityClass));
        return this;
    }

    @Override
    protected EntityViewQueryPart buildQuery(EntityViewQueryBuilderContext ctx) {
        if (!first) {
            ctx.getWhereExpressionBuilder().append(" and ");
        }
        buildQueryForChildren(ctx);
        return this;
    }

    @Override
    protected Specification<?> buildSpecification(EntityViewQueryBuilderContext ctx) {
        return CompoundOperatorSpecification.and(buildSpecificationsForChildren(ctx));
    }
}