/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.repeat;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class LpadRepeatFunction extends AbstractRepeatFunction {

    public LpadRepeatFunction() {
        super("lpad('',?1,?2)");
    }

}
