/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.builder;

import com.blazebit.persistence.deltaspike.data.base.builder.part.OrderByQueryAttribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.QueryBuilderContext} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityViewQueryBuilderContext {

    private final StringBuilder whereExpressionBuilder = new StringBuilder();
    private final List<OrderByQueryAttribute> orderByAttributes = new ArrayList<>();
    private int counter = 1;

    public EntityViewQueryBuilderContext reset() {
        counter = 1;
        return this;
    }

    public int increment() {
        return counter++;
    }

    public StringBuilder getWhereExpressionBuilder() {
        return whereExpressionBuilder;
    }

    public List<OrderByQueryAttribute> getOrderByAttributes() {
        return orderByAttributes;
    }
}
