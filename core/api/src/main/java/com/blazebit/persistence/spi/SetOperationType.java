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
 * The possible set operation types.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public enum SetOperationType {
    /**
     * The UNION set operation.
     */
    UNION,
    /**
     * The UNION ALL set operation.
     */
    UNION_ALL,
    /**
     * The INTERSECT set operation.
     */
    INTERSECT,
    /**
     * The INTERSECT ALL set operation.
     */
    INTERSECT_ALL,
    /**
     * The EXCEPT set operation.
     */
    EXCEPT,
    /**
     * The EXCEPT ALL set operation.
     */
    EXCEPT_ALL;
}