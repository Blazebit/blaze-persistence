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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.apache.deltaspike.data.impl.util.QueryUtils.splitByKeyword;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.part.OrderByQueryPart} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityViewOrderByQueryPart extends EntityViewBasePropertyQueryPart {
    private final List<OrderByQueryAttribute> attributes = new LinkedList<>();

    @Override
    public EntityViewQueryPart build(String queryPart, String method, Class<?> repositoryClass, Class<?> entityClass) {
        Set<String> collect = new LinkedHashSet<String>();
        List<String> ascSplit = new LinkedList<String>();
        split(queryPart, OrderByQueryAttribute.KEYWORD_ASC, ascSplit);
        for (String ascPart : ascSplit) {
            split(ascPart, OrderByQueryAttribute.KEYWORD_DESC, collect);
        }
        for (String part : collect) {
            OrderByQueryAttribute.Direction direction = OrderByQueryAttribute.Direction.fromQueryPart(part);
            String attribute = direction.attribute(part);
            validate(attribute, method, repositoryClass, entityClass);
            attributes.add(new OrderByQueryAttribute(attribute, direction));
        }
        return this;
    }

    @Override
    protected EntityViewOrderByQueryPart buildQuery(EntityViewQueryBuilderContext ctx) {
        ctx.getOrderByAttributes().addAll(attributes);
        return this;
    }

    @Override
    protected Specification<?> buildSpecification(EntityViewQueryBuilderContext ctx) {
        return null;
    }

    private void split(String queryPart, String keyword, Collection<String> result) {
        for (String part : splitByKeyword(queryPart, keyword)) {
            String attribute = !part.endsWith(OrderByQueryAttribute.KEYWORD_DESC) && !part.endsWith(OrderByQueryAttribute.KEYWORD_ASC) ? part + keyword : part;
            result.add(attribute);
        }
    }
}