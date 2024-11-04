/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.minute;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLMinuteAddFunction extends MinuteAddFunction {

    public MSSQLMinuteAddFunction() {
        super("(select DATEADD(minute, t2, t1) from (values (convert(DATETIME2,?1),?2)) as temp(t1, t2))");
    }

}
