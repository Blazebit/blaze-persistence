/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.hour;

import com.blazebit.persistence.impl.function.trunc.TruncFunction;
/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public abstract class TruncHourFunction extends TruncFunction {

    public static final String NAME = "TRUNC_HOUR";

    public TruncHourFunction(String template) {
        super(NAME, template);
    }

}
