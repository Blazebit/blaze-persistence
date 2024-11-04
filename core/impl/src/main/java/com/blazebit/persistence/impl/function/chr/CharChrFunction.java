/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.chr;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class CharChrFunction extends ChrFunction {

    @Override
    public String getEncodedString(String integer) {
        return "char(" + integer + ")";
    }

}