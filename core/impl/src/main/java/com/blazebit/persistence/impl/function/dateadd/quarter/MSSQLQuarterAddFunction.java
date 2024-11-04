/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.quarter;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLQuarterAddFunction extends QuarterAddFunction {

    public MSSQLQuarterAddFunction() {
        super("(select DATEADD(quarter, t2, t1) from (values (?1,?2)) as temp(t1, t2))");
    }

}
