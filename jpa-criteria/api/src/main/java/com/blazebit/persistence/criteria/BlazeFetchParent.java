/*
 * Copyright 2014 - 2016 Blazebit.
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
