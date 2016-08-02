package com.blazebit.persistence.criteria;

import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

public interface BlazeFetchParent<Z, X> extends FetchParent<Z, X> {

    /* Aliased fetch joins */

    <Y> BlazeJoin<X, Y> fetch(SingularAttribute<? super X, Y> attribute, String alias);

    <Y> BlazeJoin<X, Y> fetch(SingularAttribute<? super X, Y> attribute, String alias, JoinType jt);

    <Y> BlazeJoin<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, String alias);

    <Y> BlazeJoin<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, String alias, JoinType jt);

    @SuppressWarnings("hiding")
    <X, Y> BlazeJoin<X, Y> fetch(String attributeName, String alias);

    @SuppressWarnings("hiding")
    <X, Y> BlazeJoin<X, Y> fetch(String attributeName, String alias, JoinType jt);

    /* Covariant overrides */

    <Y> BlazeJoin<X, Y> fetch(SingularAttribute<? super X, Y> attribute);

    <Y> BlazeJoin<X, Y> fetch(SingularAttribute<? super X, Y> attribute, JoinType jt);

    <Y> BlazeJoin<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute);

    <Y> BlazeJoin<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, JoinType jt);

    @SuppressWarnings("hiding")
    <X, Y> BlazeJoin<X, Y> fetch(String attributeName);

    @SuppressWarnings("hiding")
    <X, Y> BlazeJoin<X, Y> fetch(String attributeName, JoinType jt);
}
