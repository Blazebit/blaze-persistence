/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.hour;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLHourAddFunction extends HourAddFunction {

    public MSSQLHourAddFunction() {
        super("(select DATEADD(hour, t2, t1) from (values (convert(DATETIME2,?1),?2)) as temp(t1, t2))");
    }

}
