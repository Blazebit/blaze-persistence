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

import javax.persistence.criteria.Path;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * An extended version of {@link Path}.
 *
 * @param <X> The target type
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazePath<X> extends Path<X>, BlazeExpression<X> {

    /* Covariant overrides */

    /**
     * Like {@link Path#getParentPath} but returns the subtype {@link BlazePath} instead.
     *
     * @return parent
     */
    BlazePath<?> getParentPath();

    /**
     * Like {@link Path#get(SingularAttribute)} but returns the subtype {@link BlazePath} instead.
     *
     * @param attribute single-valued attribute
     * @param <Y>       The attribute type
     * @return path corresponding to the referenced attribute
     */
    <Y> BlazePath<Y> get(SingularAttribute<? super X, Y> attribute);

    /**
     * Like {@link Path#get(PluralAttribute)} but returns the subtype {@link BlazeExpression} instead.
     *
     * @param collection collection-valued attribute
     * @param <C>        The attribute collection type
     * @param <E>        The attribute collection element type
     * @return expression corresponding to the referenced attribute
     */
    <E, C extends java.util.Collection<E>> BlazeExpression<C> get(PluralAttribute<X, C, E> collection);

    /**
     * Like {@link Path#get(MapAttribute)} but returns the subtype {@link BlazeExpression} instead.
     *
     * @param map map-valued attribute
     * @param <M> The attribute map type
     * @param <K> The attribute map key type
     * @param <V> The attribute map element type
     * @return expression corresponding to the referenced attribute
     */
    <K, V, M extends java.util.Map<K, V>> BlazeExpression<M> get(MapAttribute<X, K, V> map);

    /**
     * Like {@link Path#type} but returns the subtype {@link BlazeExpression} instead.
     *
     * @return expression corresponding to the type of the path
     */
    BlazeExpression<Class<? extends X>> type();

    /**
     * Like {@link Path#get(String)} but returns the subtype {@link BlazePath} instead.
     *
     * @param attributeName name of the attribute
     * @param <Y>           The attribute type
     * @return path corresponding to the referenced attribute
     */
    <Y> BlazePath<Y> get(String attributeName);

}
