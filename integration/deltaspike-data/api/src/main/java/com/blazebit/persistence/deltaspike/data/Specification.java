/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * A type to represent a specific view on data, heavily inspired by Spring Data's <code>Specification</code>.
 *
 * @param <T> The entity type
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface Specification<T> {

    /**
     * Creates a WHERE clause for a query of the referenced entity in form of a {@link Predicate} for the given
     * {@link Root} and {@link CriteriaQuery}.
     *
     * @param root  The query root
     * @param query The criteria query
     * @return a {@link Predicate}, may be <code>null</code>
     */
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);
}
