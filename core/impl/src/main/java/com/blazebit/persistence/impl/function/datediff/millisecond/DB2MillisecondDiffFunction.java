/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.impl.function.datediff.millisecond;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class DB2MillisecondDiffFunction extends MillisecondDiffFunction {

    public DB2MillisecondDiffFunction() {
        // NOTE: we need lateral, otherwise the alias will be lost in the subquery
        super("(select cast((days(t2) - days(t1)) as bigint) * " + (24 * 60 * 60 * 1000) + " + (midnight_seconds(t2) - midnight_seconds(t1)) * 1000 + (microsecond(t2) - microsecond(t1)) / 1000 from lateral(values (cast(?1 as timestamp), cast(?2 as timestamp))) as temp(t1,t2))");
    }
}
