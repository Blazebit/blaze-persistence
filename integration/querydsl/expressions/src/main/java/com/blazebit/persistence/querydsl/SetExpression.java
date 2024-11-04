/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.querydsl;

import com.querydsl.core.Fetchable;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.SubQueryExpression;

import javax.annotation.Nonnegative;

/**
 * Set expresion defines an interface for set operation queries.
 * Analog to {@link com.querydsl.sql.Union}, but also used for {@code INTERSECT} and {@code EXCEPT} operations.
 *
 * @param <RT> return type of the projection
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public interface SetExpression<RT> extends SubQueryExpression<RT>, Fetchable<RT>, ExtendedFetchable<RT> {

    /**
     * Set the limit / max results for the query results
     *
     * @param limit max rows
     * @return the current object
     */
    SetExpression<RT> limit(@Nonnegative long limit);

    /**
     * Set the offset for the query results
     *
     * @param offset row offset
     * @return the current object
     */
    SetExpression<RT> offset(@Nonnegative long offset);

    /**
     * Define the ordering of the query results
     *
     * @param o order
     * @return the current object
     */
    SetExpression<RT> orderBy(OrderSpecifier<?>... o);

}
