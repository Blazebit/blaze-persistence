/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.base64;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class PostgreSQLBase64Function extends Base64Function {

    @Override
    public String getEncodedString(String bytes) {
        return "encode(" + bytes + ", 'base64')";
    }

}