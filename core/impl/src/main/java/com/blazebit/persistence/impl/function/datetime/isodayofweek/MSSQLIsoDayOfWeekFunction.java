/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.isodayofweek;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLIsoDayOfWeekFunction extends IsoDayOfWeekFunction {

    /*
    DATEFIRST 7 => DATEFIRST 1

    1 = SUN     => 7 = SUN
    2 = MON     => 1 = MON
    3 = TUE     => 2 = TUE
    4 = WED     => 3 = WED
    5 = THU     => 4 = THU
    6 = FRI     => 5 = FRI
    7 = SAT     => 6 = SAT

    (X + DATEFIRST + 5) % 7 + 1
    (7 + 1 + 5    ) % 7 + 1 = 7
    (7 + 2 + 5    ) % 7 + 1 = 1
    (7 + 3 + 5    ) % 7 + 1 = 2
    (7 + 4 + 5    ) % 7 + 1 = 3
    (7 + 5 + 5    ) % 7 + 1 = 4
    (7 + 6 + 5    ) % 7 + 1 = 5
    (7 + 7 + 5    ) % 7 + 1 = 6
     */
    public MSSQLIsoDayOfWeekFunction() {
        super("((datepart(dw, convert(date, ?1)) + @@DATEFIRST + 5) % 7 + 1)");
    }
}
