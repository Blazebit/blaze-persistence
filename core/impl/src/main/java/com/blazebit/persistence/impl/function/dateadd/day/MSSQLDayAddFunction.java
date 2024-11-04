/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.day;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLDayAddFunction extends DayAddFunction {

    public MSSQLDayAddFunction() {
        super("(select DATEADD(day, t2, t1) from (values (?1,?2)) as temp(t1, t2))");
    }

}
