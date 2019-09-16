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

package com.blazebit.persistence.impl.function.datetime.epochmilli;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class DB2EpochMillisecondFunction extends EpochMillisecondFunction {

    public DB2EpochMillisecondFunction() {
        super("(select cast(DAYS(cast(t1 as timestamp))-DAYS('1970-01-01') as bigint) * " + (24L * 60L * 60L * 1000L) + " + MIDNIGHT_SECONDS(cast(t1 as timestamp)) * 1000 + MICROSECOND(t1) / 1000 from lateral(values (cast(?1 as timestamp))) as temp(t1))");
    }
}
