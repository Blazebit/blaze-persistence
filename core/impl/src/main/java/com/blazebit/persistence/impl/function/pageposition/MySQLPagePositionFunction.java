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
public class MySQLPagePositionFunction extends PagePositionFunction {

    public MySQLPagePositionFunction() {
        super("(select rownumber_ from (select @i:=@i+1 as rownumber_, base_.* from (?1) as base_, (SELECT @i:=0) as iter_) as base1_ where ?2 = base1_.?3)");
    }

}
