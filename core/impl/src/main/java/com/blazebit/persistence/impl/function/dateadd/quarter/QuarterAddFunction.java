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

package com.blazebit.persistence.impl.function.dateadd.quarter;

import com.blazebit.persistence.impl.function.dateadd.DateAddFunction;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class QuarterAddFunction extends DateAddFunction {

    public static final String NAME = "ADD_QUARTER";

    public QuarterAddFunction() {
        this("DATE_ADD(?1, INTERVAL ?2 QUARTER)");
    }

    public QuarterAddFunction(String template) {
        super(NAME, template);
    }

}
