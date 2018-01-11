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
import com.blazebit.persistence.deltaspike.data.impl.meta.EntityViewRepositoryComponent;

import java.util.LinkedList;
import java.util.List;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.part.QueryPart} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
abstract class EntityViewQueryPart {
    protected List<EntityViewQueryPart> children = new LinkedList<>();

    protected abstract EntityViewQueryPart build(String queryPart, String method, EntityViewRepositoryComponent repo);

    protected abstract EntityViewQueryPart buildQuery(EntityViewQueryBuilderContext ctx);

    protected void buildQueryForChildren(EntityViewQueryBuilderContext ctx) {
        for (EntityViewQueryPart child : children) {
            child.buildQuery(ctx);
        }
    }
}