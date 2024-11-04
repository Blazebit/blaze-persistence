/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.second;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLSecondAddFunction extends SecondAddFunction {

    public MSSQLSecondAddFunction() {
        super("(select DATEADD(second, t2, t1) from (values (convert(DATETIME2,?1),?2)) as temp(t1, t2))");
    }

}
