/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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