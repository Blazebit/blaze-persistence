/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import java.util.Collection;
import java.util.List;

/**
 * A chain of attribute de-references.
 *
 * @param <X> The type of the entity view that is the base of the path
 * @param <B> The result base type of attribute path to resolve against
 * @param <Y> The result type of attribute path
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface AttributePath<X, B, Y> {

    /**
     * Returns the path as dot separated string.
     *
     * @return The path
     */
    String getPath();

    /**
     * Returns the de-referenced attribute names in order.
     *
     * @return The de-referenced attribute names
     */
    List<String> getAttributeNames();

    /**
     * Returns the de-referenced attributes in order.
     *
     * @return The de-referenced attributes
     * @throws UnsupportedOperationException When this is a string based path
     */
    List<MethodAttribute<?, ?>> getAttributes();

    /**
     * Returns a new attribute path that additionally de-references the given path.
     *
     * @param attributePath The path to additionally de-reference
     * @param <E> The element type
     * @return The new attribute path
     */
    <E> AttributePath<X, E, E> get(String attributePath);

    /**
     * Returns a new attribute path that additionally de-references the given path.
     *
     * @param attributePath The path to additionally de-reference
     * @param <E> The element type
     * @param <C> The element base type of attribute path to resolve against
     * @return The new attribute path
     */
    <E, C extends Collection<E>> AttributePath<X, E, C> getMulti(String attributePath);

    /**
     * Returns a new attribute path that additionally de-references the given attribute.
     *
     * @param attribute The attribute to additionally de-reference
     * @param <E> The element type
     * @return The new attribute path
     */
    <E> AttributePath<X, E, E> get(MethodSingularAttribute<B, E> attribute);

    /**
     * Returns a new attribute path that additionally de-references the given attribute.
     *
     * @param attribute The attribute to additionally de-reference
     * @param <E> The element type
     * @return The new attribute path
     */
    <E> AttributePath<X, E, E> get(MethodPluralAttribute<B, ?, E> attribute);

    /**
     * Returns a new attribute path that additionally de-references the given multi-list attribute.
     *
     * @param attribute The attribute to additionally de-reference
     * @param <C> The element type to resolve against
     * @param <E> The element type
     * @return The new attribute path
     */
    <C extends Collection<E>, E> AttributePath<X, E, C> get(MethodMultiListAttribute<B, E, C> attribute);

    /**
     * Returns a new attribute path that additionally de-references the given multi-map attribute.
     *
     * @param attribute The attribute to additionally de-reference
     * @param <C> The element type to resolve against
     * @param <E> The element type
     * @return The new attribute path
     */
    <C extends Collection<E>, E> AttributePath<X, E, C> get(MethodMultiMapAttribute<B, ?, E, C> attribute);

    /**
     * Returns a new attribute path that additionally de-references the given path.
     *
     * @param attributePath The path to additionally de-reference
     * @param <C> The element type to resolve against
     * @param <E> The element type
     * @return The new attribute path
     */
    <C, E> AttributePath<X, C, E> get(AttributePath<B, C, E> attributePath);

    /**
     * Resolves this attribute path on the given base type and returns the last attribute.
     *
     * @param baseType The base type
     * @return The last attribute
     */
    MethodAttribute<?, Y> resolve(ManagedViewType<X> baseType);

    /**
     * Resolves this attribute path on the given base type and returns all attributes in order.
     *
     * @param baseType The base type
     * @return The attributes in order
     */
    List<MethodAttribute<?, ?>> resolveAll(ManagedViewType<X> baseType);
}
