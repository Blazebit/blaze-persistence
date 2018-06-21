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

package com.blazebit.persistence.criteria.impl.support;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Predicate;

/**
 * An interface necessary to be able to compile against JPA 2.0 but let users use JPA 2.1 APIs.
 *
 * @param <Z> the source type of the join
 * @param <K> the key type of the target <code>Map</code>
 * @param <V> the element type of the target <code>Map</code>
 * @author Christian Beikov
 * @since 1.3.0
 */
public interface MapJoinSupport<Z, K, V> extends MapJoin<Z, K, V> {

    MapJoin<Z, K, V> on(Expression<Boolean> restriction);

    MapJoin<Z, K, V> on(Predicate... restrictions);

}
