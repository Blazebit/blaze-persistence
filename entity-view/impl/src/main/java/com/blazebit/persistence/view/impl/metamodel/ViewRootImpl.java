/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.metamodel.OrderByItem;
import com.blazebit.persistence.view.metamodel.ViewRoot;

import javax.persistence.metamodel.Type;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
public class ViewRootImpl implements ViewRoot {

    private final String name;
    private final Type<?> type;
    private final CorrelationProviderFactory correlationProviderFactory;
    private final Class<? extends CorrelationProvider> correlationProvider;
    private final JoinType joinType;
    private final String[] fetches;
    private final List<OrderByItem> orderByItems;
    private final String limitExpression;
    private final String offsetExpression;

    public ViewRootImpl(String name, Type<?> type, CorrelationProviderFactory correlationProviderFactory, Class<? extends CorrelationProvider> correlationProvider, JoinType joinType, String[] fetches, List<OrderByItem> orderByItems, String limitExpression, String offsetExpression) {
        this.name = name;
        this.type = type;
        this.correlationProviderFactory = correlationProviderFactory;
        this.correlationProvider = correlationProvider;
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
    public Type<?> getType() {
        return type;
    }

    @Override
    public CorrelationProviderFactory getCorrelationProviderFactory() {
        return correlationProviderFactory;
    }

    @Override
    public Class<? extends CorrelationProvider> getCorrelationProvider() {
        return correlationProvider;
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
    public List<OrderByItem> getOrderByItems() {
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

    @Override
    public boolean equals(Object o) {
        return o instanceof ViewRoot && getName().equals(((ViewRoot) o).getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
