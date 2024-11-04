/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.base.builder.part;

import com.blazebit.persistence.deltaspike.data.Specification;
import com.blazebit.persistence.deltaspike.data.base.builder.EntityViewQueryBuilderContext;

import static org.apache.deltaspike.data.impl.util.QueryUtils.splitByKeyword;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.part.OrQueryPart} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityViewOrQueryPart extends EntityViewConnectingQueryPart {

    public EntityViewOrQueryPart(boolean first) {
        super(first);
    }

    @Override
    public EntityViewQueryPart build(String queryPart, String method, Class<?> repositoryClass, Class<?> entityClass) {
        String[] andParts = splitByKeyword(queryPart, "And");
        boolean first = true;
        for (String and : andParts) {
            EntityViewAndQueryPart andPart = new EntityViewAndQueryPart(first);
            first = false;
            children.add(andPart.build(and, method, repositoryClass, entityClass));
        }
        return this;
    }

    @Override
    protected EntityViewQueryPart buildQuery(EntityViewQueryBuilderContext ctx) {
        if (!first) {
            ctx.getWhereExpressionBuilder().append(" or ");
        }
        buildQueryForChildren(ctx);
        return this;
    }

    @Override
    protected Specification<?> buildSpecification(EntityViewQueryBuilderContext ctx) {
        return CompoundOperatorSpecification.or(buildSpecificationsForChildren(ctx));
    }
}