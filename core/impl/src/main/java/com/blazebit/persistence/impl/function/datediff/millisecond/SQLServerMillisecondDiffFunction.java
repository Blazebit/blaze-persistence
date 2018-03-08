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
 * @author Moritz Becker
 * @since 1.2.0
 */
public class SQLServerMillisecondDiffFunction extends MillisecondDiffFunction {

    public SQLServerMillisecondDiffFunction() {
        super("(select cast(datediff(ss,t1,t2) as bigint) * 1000 + cast(datepart(ms,t2) as bigint) - cast(datepart(ms,t1) as bigint) from (values (cast(?1 as datetime),cast(?2 as datetime))) as temp(t1,t2))");
    }
}
