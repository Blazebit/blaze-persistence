/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.spi.EntityViewRootMapping;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
public class EntityViewRootMappingImpl implements EntityViewRootMapping {

    private final String name;
    private final Class<?> managedTypeClass;
    private final String joinExpression;
    private final Class<? extends CorrelationProvider> correlationProvider;
    private final String conditionExpression;
    private final JoinType joinType;
    private final String[] fetches;
    private final List<String> orderByItems;
    private final String limitExpression;
    private final String offsetExpression;

    public EntityViewRootMappingImpl(String name, Class<?> managedTypeClass, String joinExpression, Class<? extends CorrelationProvider> correlationProvider, String conditionExpression, JoinType joinType, String[] fetches, List<String> orderByItems, String limitExpression, String offsetExpression) {
        this.name = name;
        this.managedTypeClass = managedTypeClass;
        this.joinExpression = joinExpression;
        this.correlationProvider = correlationProvider;
        this.conditionExpression = conditionExpression;
        this.joinType = joinType;
        this.fetches = fetches;
        this.orderByItems = orderByItems;
        this.limitExpression = limitExpression;
        this.offsetExpression = offsetExpression;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getManagedTypeClass() {
        return managedTypeClass;
    }

    @Override
    public String getJoinExpression() {
        return joinExpression;
    }

    @Override
    public Class<? extends CorrelationProvider> getCorrelationProvider() {
        return correlationProvider;
    }

    @Override
    public String getConditionExpression() {
        return conditionExpression;
    }

    @Override
    public JoinType getJoinType() {
        return joinType;
    }

    @Override
    public String[] getFetches() {
        return fetches;
    }

    @Override
    public List<String> getOrderByItems() {
        return orderByItems;
    }

    @Override
    public String getLimitExpression() {
        return limitExpression;
    }

    @Override
    public String getOffsetExpression() {
        return offsetExpression;
    }
}
