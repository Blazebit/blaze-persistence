/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.VersionBasicUserType;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CalendarBasicUserType implements BasicUserType<Calendar>, VersionBasicUserType<Calendar> {

    public static final BasicUserType<?> INSTANCE = new CalendarBasicUserType();

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
    public boolean isEqual(Calendar initial, Calendar current) {
        return initial.equals(current);
    }

    @Override
    public boolean isDeepEqual(Calendar initial, Calendar current) {
        return initial.equals(current);
    }

    @Override
    public int hashCode(Calendar object) {
        return object.hashCode();
    }

    @Override
    public boolean shouldPersist(Calendar entity) {
        return false;
    }

    @Override
    public String[] getDirtyProperties(Calendar entity) {
        return DIRTY_MARKER;
    }

    @Override
    public Calendar deepClone(Calendar object) {
        return object == null ? null : (Calendar) object.clone();
    }

    @Override
    public Calendar nextValue(Calendar current) {
        return Calendar.getInstance();
    }

    @Override
    public Calendar fromString(CharSequence sequence) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(Timestamp.valueOf(sequence.toString()));
        return instance;
    }

    @Override
    public String toStringExpression(String expression) {
        return "TIMESTAMP_ISO(" + expression + ")";
    }
}
