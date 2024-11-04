/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.hour;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class AccessHourFunction extends HourFunction {

    public AccessHourFunction() {
        super("datepart('h', ?1)");
    }
}
