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
     * 
     * @since 1.1.0
     */
    public static final String RETURNING_CLAUSE_CASE_SENSITIVE = "com.blazebit.persistence.returning_clause_case_sensitive";

    private ConfigurationProperties() {
    }
}
