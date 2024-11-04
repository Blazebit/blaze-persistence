/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import java.sql.Time;
import java.util.Calendar;

import com.blazebit.persistence.parser.CharSequenceUtils;
import com.blazebit.persistence.view.spi.type.BasicUserType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TimeBasicUserType implements BasicUserType<Time> {

    public static final BasicUserType<?> INSTANCE = new TimeBasicUserType();

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public boolean supportsDirtyChecking() {
        return false;
    }

    @Override
    public boolean supportsDirtyTracking() {
        return false;
    }

    @Override
    public boolean supportsDeepEqualChecking() {
        return true;
    }

    @Override
    public boolean supportsDeepCloning() {
        return true;
    }

    @Override
    public boolean isEqual(Time initial, Time current) {
        return initial.getTime() == current.getTime();
    }

    @Override
    public boolean isDeepEqual(Time initial, Time current) {
        return initial.getTime() == current.getTime();
    }

    @Override
    public int hashCode(Time object) {
        return object.hashCode();
    }

    @Override
    public boolean shouldPersist(Time entity) {
        return false;
    }

    @Override
    public String[] getDirtyProperties(Time entity) {
        return DIRTY_MARKER;
    }

    @Override
    public Time deepClone(Time object) {
        return (Time) object.clone();
    }

    @Override
    public Time fromString(CharSequence sequence) {
        int fractionDot = CharSequenceUtils.lastIndexOf(sequence, '.');
        if (fractionDot == -1) {
            return Time.valueOf(sequence.toString());
        }
        int millisEndIndex = fractionDot + 4;
        Time t = Time.valueOf(sequence.subSequence(0, fractionDot).toString());
        Calendar c = Calendar.getInstance();
        c.setTime(t);
        String fractions = sequence.subSequence(fractionDot + 1, millisEndIndex).toString();
        c.set(Calendar.MILLISECOND, Integer.parseInt(fractions));
        return new Time(c.getTimeInMillis());
    }

    @Override
    public String toStringExpression(String expression) {
        return "TIME_ISO(" + expression + ")";
    }
}
