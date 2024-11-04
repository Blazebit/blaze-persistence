/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.timeiso;

/**
 * @author Christian Beikov
 * @since 1.6.12
 */
public class PostgreSQLTimeIsoFunction extends TimeIsoFunction {
    @Override
    protected String getExpression(String timestampArgument) {
        return "to_char(" + timestampArgument + ", 'HH24:MI:SS.US')";
    }
}