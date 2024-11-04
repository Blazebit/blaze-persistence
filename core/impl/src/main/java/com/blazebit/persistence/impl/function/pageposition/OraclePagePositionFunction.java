/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.pageposition;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class OraclePagePositionFunction extends PagePositionFunction {

    private static final String ROWNUM = "rownum";

    public OraclePagePositionFunction() {
        // Oracle doesn't treat the subquery as constant...
        super("MAX((select base1_.rownumber_ from (select " + ROWNUM
                + " as rownumber_, base_.* from ?1 base_) base1_ where ?2 = base1_.?3))");
    }

    @Override
    protected String getRownumFunction() {
        return ROWNUM;
    }

}
