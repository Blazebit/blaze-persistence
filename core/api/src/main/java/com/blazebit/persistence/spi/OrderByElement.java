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
 * Represents an order by element.
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface OrderByElement {

    /**
     * The position of the order by expression in the select clause.
     *
     * @return The position
     */
    public int getPosition();

    /**
     * Whether sorting is ascending or descending.
     *
     * @return True if ascending, false otherwise
     */
    public boolean isAscending();

    /**
     * Whether the expression may produce null values.
     *
     * @return True if nullable, false otherwise
     * @since 1.3.0
     */
    public boolean isNullable();

    /**
     * Whether nulls have precedence or non-nulls.
     *
     * @return True if nulls come first, false otherwise
     */
    public boolean isNullsFirst();
    
}
