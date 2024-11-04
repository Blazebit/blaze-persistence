/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.filter;

import java.io.Serializable;

/**
 * A range for the value type.
 *
 * @param <T> The range value type
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class Range<T> implements Serializable {

    private final T lowerBound;
    private final T upperBound;
    private final boolean inclusive;

    private Range(T lowerBound, T upperBound, boolean inclusive) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.inclusive = inclusive;
    }

    /**
     * Creates a range for the values smaller than the given value.
     *
     * @param upperBound The upper bound
     * @param <T> The value type
     * @return The range
     */
    public static <T> Range<T> lt(T upperBound) {
        return new Range<>(null, upperBound, false);
    }

    /**
     * Creates a range for the values smaller or equal than the given value.
     *
     * @param upperBound The upper bound
     * @param <T> The value type
     * @return The range
     */
    public static <T> Range<T> le(T upperBound) {
        return new Range<>(null, upperBound, true);
    }

    /**
     * Creates a range for the values greater than the given value.
     *
     * @param lowerBound The lower bound
     * @param <T> The value type
     * @return The range
     */
    public static <T> Range<T> gt(T lowerBound) {
        return new Range<>(lowerBound, null, false);
    }

    /**
     * Creates a range for the values greater or equal than the given value.
     *
     * @param lowerBound The lower bound
     * @param <T> The value type
     * @return The range
     */
    public static <T> Range<T> ge(T lowerBound) {
        return new Range<>(lowerBound, null, true);
    }

    /**
     * Creates a range for the values between the given lower and upper bound, the given values inclusive.
     *
     * @param lowerBound The lower bound
     * @param upperBound The upper bound
     * @param <T> The value type
     * @return The range
     */
    public static <T> Range<T> between(T lowerBound, T upperBound) {
        return new Range<>(lowerBound, upperBound, true);
    }

    /**
     * Creates a range for the values between the given lower and upper bound, the given values exclusive.
     *
     * @param lowerBound The lower bound
     * @param upperBound The upper bound
     * @param <T> The value type
     * @return The range
     */
    public static <T> Range<T> betweenExclusive(T lowerBound, T upperBound) {
        return new Range<>(lowerBound, upperBound, false);
    }

    /**
     * Returns the lower bound, or null if unbounded.
     *
     * @return The lower bound
     */
    public T getLowerBound() {
        return lowerBound;
    }

    /**
     * Returns the upper bound, or null if unbounded.
     *
     * @return The upper bound
     */
    public T getUpperBound() {
        return upperBound;
    }

    /**
     * Returns whether the bounds are inclusive.
     *
     * @return whether the bounds are inclusive
     */
    public boolean isInclusive() {
        return inclusive;
    }
}
