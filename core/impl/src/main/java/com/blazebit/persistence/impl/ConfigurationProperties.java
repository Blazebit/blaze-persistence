/*
 * Copyright 2014 Blazebit.
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

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public final class ConfigurationProperties {

    /**
     * We added a flag to enable a JPA compatible mode because we allow to make use of many vendor
     * specific extensions which maybe aren't portable. By enabling the compatible mode functionality
     * is restricted but more portable.
     * By default the compatible mode is disabled because most JPA providers support the same extensions.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     * 
     * @since 1.0.5
     */
    public static final String COMPATIBLE_MODE = "com.blazebit.persistence.compatible_mode";
    
    /**
     * Some databases require case sensitivity for attribute paths in the returning clause
     * (unlike PostgreSQL which requires case insensitivity for column names in returning clause)
     * By default the returning clause is case sensitive.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     * 
     * @since 1.1.0
     */
    public static final String RETURNING_CLAUSE_CASE_SENSITIVE = "com.blazebit.persistence.returning_clause_case_sensitive";

    /**
     * If set to false, uses of SIZE will always be transformed to subqueries.
     * By default the size to count transformation is enabled.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     * Default is <code>true</code>
     * 
     * @since 1.1.0
     */
    public static final String SIZE_TO_COUNT_TRANSFORMATION = "com.blazebit.persistence.size_to_count_transformation";
    
    /**
     * If set to false, no implicit group by clauses will be generated from the SELECT part of the query.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     * Default is <code>true</code>
     * 
     * @since 1.1.0
     */
    public static final String IMPLICIT_GROUP_BY_FROM_SELECT = "com.blazebit.persistence.implicit_group_by_from_select";
    
    /**
     * If set to false, no implicit group by clauses will be generated from the HAVING part of the query.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     * Default is <code>true</code>
     * 
     * @since 1.1.0
     */
    public static final String IMPLICIT_GROUP_BY_FROM_HAVING = "com.blazebit.persistence.implicit_group_by_from_having";
    
    /**
     * If set to false, no implicit group by clauses will be generated from the ORDER BY part of the query.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     * Default is <code>true</code>
     * 
     * @since 1.1.0
     */
    public static final String IMPLICIT_GROUP_BY_FROM_ORDER_BY = "com.blazebit.persistence.implicit_group_by_from_order_by";

    /**
     * If set to false, no expression optimization takes place.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     * Default is <code>true</code>
     *
     * @since 1.1.0
     */
    public static final String EXPRESSION_OPTIMIZATION = "com.blazebit.persistence.expression_optimization";
    
    private ConfigurationProperties() {
    }
}
