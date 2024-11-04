/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateiso;

/**
 * @author Christian Beikov
 * @since 1.6.12
 */
public class MySQLDateIsoFunction extends DateIsoFunction {
    @Override
    protected String getExpression(String timestampArgument) {
        return "date_format(" + timestampArgument + ", '%Y-%m-%d')";
    }

}