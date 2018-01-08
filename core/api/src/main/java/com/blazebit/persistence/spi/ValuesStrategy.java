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

package com.blazebit.persistence.spi;

/**
 * Strategies for generating a VALUES table reference.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public enum ValuesStrategy {
    VALUES,
    SELECT_VALUES,
    SELECT_UNION;

    // NOTE: another possible strategy would be to use a temporary table
    // CREATE TEMPORARY TABLE IF NOT EXISTS table2 AS (SELECT * FROM table1)
}
