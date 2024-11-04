/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import java.util.Map;

/**
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public interface QueryConfiguration {

    public boolean isCompatibleModeEnabled();

    public boolean isReturningClauseCaseSensitive();

    public boolean isExpressionOptimizationEnabled();

    public String getExpressionCacheClass();

    public boolean isCountTransformationEnabled();

    public boolean isImplicitGroupByFromSelectEnabled();

    public boolean isImplicitGroupByFromHavingEnabled();

    public boolean isImplicitGroupByFromOrderByEnabled();

    public boolean isValuesClauseFilterNullsEnabled();

    public boolean isParameterAsLiteralRenderingEnabled();

    public boolean isOptimizedKeysetPredicateRenderingEnabled();

    public Boolean getInlineIdQueryEnabled();

    public Boolean getInlineCountQueryEnabled();

    public Boolean getInlineCtesEnabled();

    public String getProperty(String name);

    public Map<String, String> getProperties();

    public void setProperties(Map<String, String> properties);

    public void setProperty(String propertyName, String propertyValue);

    public void setCacheable(boolean cacheable);

    public boolean isCacheable();

    public boolean isQueryPlanCacheEnabled();
}
