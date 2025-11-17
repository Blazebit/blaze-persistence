/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.MapAttribute;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;

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
    <E, C extends java.util.Collection<E>> BlazeExpression<C> get(PluralAttribute<? super X, C, E> collection);

    /**
     * Like {@link Path#get(MapAttribute)} but returns the subtype {@link BlazeExpression} instead.
     *
     * @param map map-valued attribute
     * @param <M> The attribute map type
     * @param <K> The attribute map key type
     * @param <V> The attribute map element type
     * @return expression corresponding to the referenced attribute
     */
    <K, V, M extends java.util.Map<K, V>> BlazeExpression<M> get(MapAttribute<? super X, K, V> map);

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
