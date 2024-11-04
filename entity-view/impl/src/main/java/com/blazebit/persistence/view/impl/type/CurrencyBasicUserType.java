/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

import java.util.Currency;

/**
 *
 * @author Christian Beikov
 * @since 1.6.3
 */
public class CurrencyBasicUserType extends ImmutableBasicUserType<Currency> {

    public static final BasicUserType<Currency> INSTANCE = new CurrencyBasicUserType();

    @Override
    public Currency fromString(CharSequence sequence) {
        return Currency.getInstance(sequence.toString());
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
