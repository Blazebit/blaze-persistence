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

package com.blazebit.persistence.impl.function.datediff.microsecond;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2MicrosecondDiffFunction extends MicrosecondDiffFunction {

    public DB2MicrosecondDiffFunction() {
        // NOTE: we need lateral, otherwise the alias will be lost in the subquery
        super("(select cast((days(t2) - days(t1)) as bigint) * (" + (24L * 60L * 60L * 1000000L) + ") + cast((midnight_seconds(t2) - midnight_seconds(t1)) as bigint) * 1000000 + (microsecond(t2) - microsecond(t1)) from lateral(values (cast(?1 as timestamp), cast(?2 as timestamp))) as temp(t1,t2))");
    }
}
