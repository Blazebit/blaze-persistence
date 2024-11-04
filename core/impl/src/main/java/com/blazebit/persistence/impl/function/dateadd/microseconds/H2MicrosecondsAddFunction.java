/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.microseconds;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class H2MicrosecondsAddFunction extends MicrosecondsAddFunction {

    public H2MicrosecondsAddFunction() {
        super("DATEADD(microseconds, ?2, ?1)");
    }

}
