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

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;

/**
 * An extended version of {@link ListJoin}.
 *
 * @param <Z> the source type of the join
 * @param <E> the element type of the target <code>List</code>
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeListJoin<Z, E> extends ListJoin<Z, E>, BlazeJoin<Z, E> {

    /**
     * Like {@link BlazeJoin#treatAs} but returns the subtype {@link BlazeListJoin} instead.
     *
     * @param type type to be downcast to
     * @param <T>  The target treat type
     * @return The treated join object
     */
    <T extends E> BlazeListJoin<Z, T> treatAs(Class<T> type);

    /* Compatibility for JPA 2.1 */

    /**
     * Modify the join to restrict the result according to the
     * specified ON condition. Replaces the previous ON condition,
     * if any.
     * Return the join object
     *
     * @param restriction a simple or compound boolean expression
     * @return the modified join object
     */
    BlazeListJoin<Z, E> on(Expression<Boolean> restriction);

    /**
     * Modify the join to restrict the result according to the
     * specified ON condition. Replaces the previous ON condition,
     * if any.
     * Return the join object
     *
     * @param restrictions zero or more restriction predicates
     * @return the modified join object
     */
    BlazeListJoin<Z, E> on(Predicate... restrictions);

}
