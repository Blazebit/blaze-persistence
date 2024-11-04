/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
