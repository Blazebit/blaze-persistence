/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.stringxmlagg;

import com.blazebit.persistence.impl.function.concat.ConcatFunction;
import com.blazebit.persistence.impl.function.groupconcat.AbstractGroupConcatFunction;
import com.blazebit.persistence.impl.function.replace.ReplaceFunction;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class OracleGroupConcatBasedStringXmlAggFunction extends GroupConcatBasedStringXmlAggFunction {

    public OracleGroupConcatBasedStringXmlAggFunction(AbstractGroupConcatFunction groupConcatFunction, ReplaceFunction replaceFunction, ConcatFunction concatFunction) {
        super(groupConcatFunction, replaceFunction, concatFunction);
    }

    @Override
    protected String coalesceStart() {
        return "nullif(";
    }

    @Override
    protected String coalesceEnd(String field) {
        return "," + concatFunction.startConcat() + "'<'" + concatFunction.concatSeparator() + field + concatFunction.concatSeparator() + "'></'" + concatFunction.concatSeparator() + field + concatFunction.concatSeparator() + "'>'" + concatFunction.endConcat() + ")";
    }

}