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

package com.blazebit.persistence.criteria;

import javax.persistence.criteria.Order;

/**
 * An extended version of {@link Order}.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeOrder extends Order {

    /**
     * Switch the null precedence.
     *
     * @return A new <code>BlazeOrder</code> instance with the reversed null precedence
     */
    BlazeOrder reverseNulls();

    /**
     * Whether nulls come first.
     *
     * @return True if nulls come first, false otherwise
     */
    boolean isNullsFirst();

    /* covariant overrides */

    /**
     * Switch the ordering.
     *
     * @return A new <code>BlazeOrder</code> instance with the reversed ordering
     */
    BlazeOrder reverse();

}
