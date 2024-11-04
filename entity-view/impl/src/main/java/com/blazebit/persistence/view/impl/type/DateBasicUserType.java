/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.VersionBasicUserType;

import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DateBasicUserType implements BasicUserType<Date>, VersionBasicUserType<Date> {

    public static final BasicUserType<?> INSTANCE = new DateBasicUserType();

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
    public boolean isEqual(Date initial, Date current) {
        return initial.getTime() == current.getTime();
    }

    @Override
    public boolean isDeepEqual(Date initial, Date current) {
        return initial.getTime() == current.getTime();
    }

    @Override
    public int hashCode(Date object) {
        return object.hashCode();
    }

    @Override
    public boolean shouldPersist(Date entity) {
        return false;
    }

    @Override
    public String[] getDirtyProperties(Date entity) {
        return DIRTY_MARKER;
    }

    @Override
    public Date deepClone(Date object) {
        return (Date) object.clone();
    }

    @Override
    public Date nextValue(Date current) {
        return new Date();
    }

    @Override
    public Date fromString(CharSequence sequence) {
        return new Date(parseTimestamp(sequence).getTime());
    }

    protected Timestamp parseTimestamp(CharSequence sequence) {
        char[] chars = new char[29];
        int length = sequence.length();
        for (int i = 0; i < length; i++) {
            chars[i] = sequence.charAt(i);
        }
        // Fill up zeros
        for (int i = length; i < chars.length; i++) {
            chars[i] = '0';
        }
        // Replace the T from the ISO format
        chars[10] = ' ';
        return java.sql.Timestamp.valueOf(new String(chars));
    }

    @Override
    public String toStringExpression(String expression) {
        return "TIMESTAMP_ISO(" + expression + ")";
    }
}
