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

package com.blazebit.persistence.deltaspike.data.base.builder.postprocessor;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.deltaspike.data.base.handler.CriteriaBuilderPostProcessor;
import org.apache.deltaspike.data.impl.builder.OrderDirection;

import javax.persistence.metamodel.SingularAttribute;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.postprocessor.OrderByQueryStringPostProcessor}
 * but was modified to work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class OrderByCriteriaBuilderPostProcessor implements CriteriaBuilderPostProcessor {

    private final String attribute;
    private OrderDirection direction;

    public OrderByCriteriaBuilderPostProcessor(SingularAttribute<?, ?> attribute, OrderDirection direction) {
        this.attribute = attribute.getName();
        this.direction = direction;
    }

    public OrderByCriteriaBuilderPostProcessor(String attribute, OrderDirection direction) {
        this.attribute = attribute;
        this.direction = direction;
    }

    @Override
    public FullQueryBuilder<?, ?> postProcess(FullQueryBuilder<?, ?> criteriaBuilder) {
        return criteriaBuilder.orderBy(attribute, direction == OrderDirection.ASC, false);
    }

    public boolean matches(SingularAttribute<?, ?> attribute) {
        return matches(attribute.getName());
    }

    public boolean matches(String attribute) {
        return this.attribute.equals(attribute);
    }

    public void changeDirection() {
        direction = direction.change();
    }
}