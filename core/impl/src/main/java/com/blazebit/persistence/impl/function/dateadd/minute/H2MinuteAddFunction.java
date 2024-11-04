/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.minute;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class H2MinuteAddFunction extends MinuteAddFunction {

    public H2MinuteAddFunction() {
        super("DATEADD(minute, ?2, ?1)");
    }

}
