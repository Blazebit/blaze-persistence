/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TimestampBasicUserType extends DateBasicUserType {

    public static final BasicUserType<Date> INSTANCE = new TimestampBasicUserType();

    @Override
    public Date nextValue(Date current) {
        return new Timestamp(System.currentTimeMillis());
    }

    @Override
    public Date fromString(CharSequence sequence) {
        return parseTimestamp(sequence);
    }

}
