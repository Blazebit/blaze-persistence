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

package com.blazebit.persistence.impl.function.dateadd.year;

import com.blazebit.persistence.impl.function.dateadd.DateAddFunction;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class YearAddFunction extends DateAddFunction {

    public static final String NAME = "ADD_YEAR";

    public YearAddFunction() {
        this("DATE_ADD(?1, INTERVAL ?2 YEAR)");
    }

    public YearAddFunction(String template) {
        super(NAME, template);
    }

}
