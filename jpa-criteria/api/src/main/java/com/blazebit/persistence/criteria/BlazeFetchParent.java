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

import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * An extended version of {@link FetchParent}.
 *
 * @param <Z> The source type
 * @param <X> The target type
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeFetchParent<Z, X> extends FetchParent<Z, X> {

    /* Aliased fetch joins */

    /**
     * Like {@link FetchParent#fetch(SingularAttribute)} but allows to set the alias of the {@link BlazeJoin}.
     *
     * @param attribute The target of the join
     * @param alias     The alias for the {@link BlazeJoin}
     * @param <Y>       The type of the join relation
     * @return The resulting fetch join
     */
    <Y> BlazeJoin<X, Y> fetch(SingularAttribute<? super X, Y> attribute, String alias);

    /**
     * Like {@link FetchParent#fetch(SingularAttribute, JoinType)} but allows to set the alias of the {@link BlazeJoin}.
     *
     * @param attribute The target of the join
     * @param alias     The alias for the {@link BlazeJoin}
     * @param jt        The join type
     * @param <Y>       The type of the join relation
     * @return The resulting fetch join
     */
    <Y> BlazeJoin<X, Y> fetch(SingularAttribute<? super X, Y> attribute, String alias, JoinType jt);

    /**
     * Like {@link FetchParent#fetch(PluralAttribute)} but allows to set the alias of the {@link BlazeJoin}.
     *
     * @param attribute The target of the join
     * @param alias     The alias for the {@link BlazeJoin}
     * @param <Y>       The type of the join relation
     * @return The resulting fetch join
     */
    <Y> BlazeJoin<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, String alias);

    /**
     * Like {@link FetchParent#fetch(PluralAttribute, JoinType)} but allows to set the alias of the {@link BlazeJoin}.
     *
     * @param attribute The target of the join
     * @param alias     The alias for the {@link BlazeJoin}
     * @param jt        The join type
     * @param <Y>       The type of the join relation
     * @return The resulting fetch join
     */
    <Y> BlazeJoin<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, String alias, JoinType jt);

    /**
     * Like {@link FetchParent#fetch(String)} but allows to set the alias of the {@link BlazeJoin}.
     *
     * @param attributeName The target of the join
     * @param alias         The alias for the {@link BlazeJoin}
     * @param <X>           The source type of the join relation
     * @param <Y>           The type of the join relation
     * @return The resulting fetch join
     */
    @SuppressWarnings("hiding")
    <X, Y> BlazeJoin<X, Y> fetch(String attributeName, String alias);

    /**
     * Like {@link FetchParent#fetch(String, JoinType)} but allows to set the alias of the {@link BlazeJoin}.
     *
     * @param attributeName The target of the join
     * @param alias         The alias for the {@link BlazeJoin}
     * @param jt            The join type
     * @param <X>           The source type of the join relation
     * @param <Y>           The type of the join relation
     * @return The resulting fetch join
     */
    @SuppressWarnings("hiding")
    <X, Y> BlazeJoin<X, Y> fetch(String attributeName, String alias, JoinType jt);

    /* Covariant overrides */

    /**
     * Like {@link FetchParent#fetch(SingularAttribute)} but returns the subtype {@link BlazeJoin} instead.
     *
     * @param attribute The target of the join
     * @param <Y>       The type of the join relation
     * @return The resulting fetch join
     */
    <Y> BlazeJoin<X, Y> fetch(SingularAttribute<? super X, Y> attribute);

    /**
     * Like {@link FetchParent#fetch(SingularAttribute, JoinType)} but returns the subtype {@link BlazeJoin} instead.
     *
     * @param attribute The target of the join
     * @param jt        The join type
     * @param <Y>       The type of the join relation
     * @return The resulting fetch join
     */
    <Y> BlazeJoin<X, Y> fetch(SingularAttribute<? super X, Y> attribute, JoinType jt);

    /**
     * Like {@link FetchParent#fetch(PluralAttribute)} but returns the subtype {@link BlazeJoin} instead.
     *
     * @param attribute The target of the join
     * @param <Y>       The type of the join relation
     * @return The resulting fetch join
     */
    <Y> BlazeJoin<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute);

    /**
     * Like {@link FetchParent#fetch(PluralAttribute, JoinType)} but returns the subtype {@link BlazeJoin} instead.
     *
     * @param attribute The target of the join
     * @param jt        The join type
     * @param <Y>       The type of the join relation
     * @return The resulting fetch join
     */
    <Y> BlazeJoin<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, JoinType jt);

    /**
     * Like {@link FetchParent#fetch(String)} but returns the subtype {@link BlazeJoin} instead.
     *
     * @param attributeName The target of the join
     * @param <X>           The source type of the join relation
     * @param <Y>           The type of the join relation
     * @return The resulting fetch join
     */
    @SuppressWarnings("hiding")
    <X, Y> BlazeJoin<X, Y> fetch(String attributeName);

    /**
     * Like {@link FetchParent#fetch(String, JoinType)} but returns the subtype {@link BlazeJoin} instead.
     *
     * @param attributeName The target of the join
     * @param jt            The join type
     * @param <X>           The source type of the join relation
     * @param <Y>           The type of the join relation
     * @return The resulting fetch join
     */
    @SuppressWarnings("hiding")
    <X, Y> BlazeJoin<X, Y> fetch(String attributeName, JoinType jt);
}
