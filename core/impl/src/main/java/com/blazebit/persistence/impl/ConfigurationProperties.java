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

package com.blazebit.persistence.impl;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
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
     * The property can be changed for a criteria builder before generating the query.
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
     * The property can be changed for a criteria builder before generating the query.
     * 
     * @since 1.1.0
     */
    public static final String SIZE_TO_COUNT_TRANSFORMATION = "com.blazebit.persistence.size_to_count_transformation";
    
    /**
     * If set to false, no implicit group by clauses will be generated from the SELECT part of the query.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     * Default is <code>true</code>
     *
     * The property can be changed for a criteria builder before generating the query.
     * 
     * @since 1.1.0
     */
    public static final String IMPLICIT_GROUP_BY_FROM_SELECT = "com.blazebit.persistence.implicit_group_by_from_select";
    
    /**
     * If set to false, no implicit group by clauses will be generated from the HAVING part of the query.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     * Default is <code>true</code>
     *
     * The property can be changed for a criteria builder before generating the query.
     * 
     * @since 1.1.0
     */
    public static final String IMPLICIT_GROUP_BY_FROM_HAVING = "com.blazebit.persistence.implicit_group_by_from_having";
    
    /**
     * If set to false, no implicit group by clauses will be generated from the ORDER BY part of the query.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     * Default is <code>true</code>
     *
     * The property can be changed for a criteria builder before generating the query.
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

    /**
     * The full qualified expression cache implementation class name.
     *
     * @since 1.2.0
     */
    public static final String EXPRESSION_CACHE_CLASS = "com.blazebit.persistence.expression.cache_class";

    /**
     * If set to false, tuples of a VALUES clause with all null values won't be filtered out.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     * Default is <code>true</code>
     *
     * The property can be changed for a criteria builder before using the VALUES clause.
     *
     * @since 1.2.0
     */
    public static final String VALUES_CLAUSE_FILTER_NULLS = "com.blazebit.persistence.values.filter_nulls";

    /**
     * If set to false, parameters are always rendered as such, otherwise the values might get inlined when no type can be inferred.
     * Valid values for this property are <code>true</code> or <code>false</code>.
     * Default is <code>true</code>
     *
     * The property can be changed for a criteria builder before constructing a query.
     *
     * @since 1.2.0
     */
    public static final String PARAMETER_AS_LITERAL_RENDERING = "com.blazebit.persistence.parameter_literal_rendering";

    /**
     * If set to true, the keyset predicate is rendered in an optimized form so that database optimizers are more likely
     * to use indices.
     * See https://github.com/Blazebit/blaze-persistence/issues/419
     * Default is <code>true</code>
     *
     * The property can be changed for a criteria builder before constructing a query.
     *
     * @since 1.2.0
     */
    public static final String OPTIMIZED_KEYSET_PREDICATE_RENDERING = "com.blazebit.persistence.optimized_keyset_predicate_rendering";
    
    private ConfigurationProperties() {
    }
}
