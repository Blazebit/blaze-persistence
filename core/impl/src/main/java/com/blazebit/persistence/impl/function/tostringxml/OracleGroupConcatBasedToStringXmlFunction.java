/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.tostringxml;

import com.blazebit.persistence.impl.function.concat.ConcatFunction;
import com.blazebit.persistence.impl.function.groupconcat.AbstractGroupConcatFunction;
import com.blazebit.persistence.impl.function.replace.ReplaceFunction;
import com.blazebit.persistence.spi.LateralStyle;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class OracleGroupConcatBasedToStringXmlFunction extends GroupConcatBasedToStringXmlFunction {

    public OracleGroupConcatBasedToStringXmlFunction(AbstractGroupConcatFunction groupConcatFunction, ReplaceFunction replaceFunction, ConcatFunction concatFunction, LateralStyle lateralStyle) {
        super(groupConcatFunction, replaceFunction, concatFunction, lateralStyle);
    }

    @Override
    protected String coalesceStart() {
        return "nullif(";
    }

    @Override
    protected String coalesceEnd(String field) {
        return ",'<" + field + "></" + field + ">')";
    }

}