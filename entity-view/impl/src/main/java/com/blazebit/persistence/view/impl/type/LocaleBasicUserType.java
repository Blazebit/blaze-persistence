/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.i18n.LocaleUtils;
import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

import java.util.Locale;

/**
 *
 * @author Christian Beikov
 * @since 1.6.3
 */
public class LocaleBasicUserType extends ImmutableBasicUserType<Locale> {

    public static final BasicUserType<Locale> INSTANCE = new LocaleBasicUserType();

    @Override
    public Locale fromString(CharSequence sequence) {
        return LocaleUtils.getLocale(sequence.toString());
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
