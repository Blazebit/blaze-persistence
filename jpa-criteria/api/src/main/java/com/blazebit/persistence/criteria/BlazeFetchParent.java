/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import jakarta.persistence.criteria.FetchParent;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;

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
