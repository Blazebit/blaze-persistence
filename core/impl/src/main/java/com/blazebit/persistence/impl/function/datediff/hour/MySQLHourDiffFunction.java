/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.hour;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class MySQLHourDiffFunction extends HourDiffFunction {

    public MySQLHourDiffFunction() {
        super("timestampdiff(HOUR, ?1, ?2)");
    }
}
