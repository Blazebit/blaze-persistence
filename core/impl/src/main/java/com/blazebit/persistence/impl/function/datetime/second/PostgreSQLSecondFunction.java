/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.second;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class PostgreSQLSecondFunction extends SecondFunction {

    public PostgreSQLSecondFunction() {
        super("cast(extract(second from ?1) as int)");
    }
}
