/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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