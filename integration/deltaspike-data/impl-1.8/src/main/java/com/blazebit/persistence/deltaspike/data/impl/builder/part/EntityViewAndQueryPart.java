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

package com.blazebit.persistence.deltaspike.data.impl.builder.part;

import com.blazebit.persistence.deltaspike.data.impl.builder.EntityViewQueryBuilderContext;
import com.blazebit.persistence.deltaspike.data.impl.meta.EntityViewAwareRepositoryMetadata;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.part.AndQueryPart} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
class EntityViewAndQueryPart extends EntityViewConnectingQueryPart {

    public EntityViewAndQueryPart(boolean first) {
        super(first);
    }

    @Override
    protected EntityViewQueryPart build(String queryPart, String method, EntityViewAwareRepositoryMetadata repo) {
        children.add(new EntityViewPropertyQueryPart().build(queryPart, method, repo));
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
}