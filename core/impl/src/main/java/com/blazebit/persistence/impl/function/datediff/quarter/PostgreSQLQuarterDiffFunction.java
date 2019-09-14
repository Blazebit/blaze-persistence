/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.impl.function.datediff.quarter;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class PostgreSQLQuarterDiffFunction extends QuarterDiffFunction {

    public PostgreSQLQuarterDiffFunction() {
        super("(select sign(i2) * floor(abs(i2)) from ( select (date_part('year', t1) * 12 + date_part('month', t1))/-3 from (values (age(?1,?2))) as temp(t1)) as temp2(i2))");
    }

}
