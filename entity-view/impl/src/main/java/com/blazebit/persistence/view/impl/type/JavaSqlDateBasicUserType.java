/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import java.sql.Date;

import com.blazebit.persistence.view.spi.type.BasicUserType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class JavaSqlDateBasicUserType implements BasicUserType<Date> {

    public static final BasicUserType<?> INSTANCE = new JavaSqlDateBasicUserType();

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
    public Date fromString(CharSequence sequence) {
        return java.sql.Date.valueOf(sequence.toString());
    }

    @Override
    public String toStringExpression(String expression) {
        return "DATE_ISO(" + expression + ")";
    }
}
