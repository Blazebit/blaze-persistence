/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.AbstractMutableBasicUserType;
import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.util.TimeZone;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TimeZoneBasicUserType extends AbstractMutableBasicUserType<TimeZone> implements BasicUserType<TimeZone> {

    public static final BasicUserType<?> INSTANCE = new TimeZoneBasicUserType();

    @Override
    public TimeZone deepClone(TimeZone object) {
        return (TimeZone) object.clone();
    }

    @Override
    public TimeZone fromString(CharSequence sequence) {
        return TimeZone.getTimeZone(sequence.toString());
    }

    @Override
    public String toStringExpression(String expression) {
        return "TO_CHAR(" + expression + ", 'TZ')";
    }
}
