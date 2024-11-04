/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.minute;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DB2MinuteFunction extends MinuteFunction {

    public DB2MinuteFunction() {
        super("minute(?1)");
    }
}
