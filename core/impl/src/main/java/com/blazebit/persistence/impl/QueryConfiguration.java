/*
 * Copyright 2014 - 2017 Blazebit.
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

    public String getProperty(String name);

    public Map<String, String> getProperties();

    public void setProperties(Map<String, String> properties);

    public void setProperty(String propertyName, String propertyValue);
}
