/*
 * Copyright 2014 - 2023 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.impl.function.datetime.dayofweek;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLDayOfWeekFunction extends DayOfWeekFunction {

    /*
     DATEFIRST 1 => DATEFIRST 7

     1 = MON     => 2 = MON
     2 = TUE     => 3 = TUE
     3 = WED     => 4 = WED
     4 = THU     => 5 = THU
     5 = FRI     => 6 = FRI
     6 = SAT     => 7 = SAT
     7 = SUN     => 1 = SUN

     (X + DATEFIRST - 1) % 7 + 1
     (1 + 1 - 1    ) % 7 + 1 = 2
     (1 + 2 - 1    ) % 7 + 1 = 3
     (1 + 3 - 1    ) % 7 + 1 = 4
     (1 + 4 - 1    ) % 7 + 1 = 5
     (1 + 5 - 1    ) % 7 + 1 = 6
     (1 + 6 - 1    ) % 7 + 1 = 7
     (1 + 7 - 1    ) % 7 + 1 = 1
     */
    public MSSQLDayOfWeekFunction() {
        super("((datepart(dw, convert(date, ?1)) + @@DATEFIRST - 1) % 7 + 1)");
    }
}
