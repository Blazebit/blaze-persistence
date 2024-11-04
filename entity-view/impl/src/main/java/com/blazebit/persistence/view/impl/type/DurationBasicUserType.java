/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

import java.time.Duration;

/**
 *
 * @author Christian Beikov
 * @since 1.6.3
 */
public class DurationBasicUserType extends ImmutableBasicUserType<Duration> {

    public static final BasicUserType<Duration> INSTANCE = new DurationBasicUserType();

    @Override
    public Duration fromString(CharSequence sequence) {
        return Duration.ofNanos(Long.valueOf(sequence.toString()));
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
