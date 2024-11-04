/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

/**
 *
 * @author Christian Beikov
 * @since 1.6.3
 */
public class EnumBasicUserType<E extends Enum<E>> extends ImmutableBasicUserType<E> {

    private final Class<E> enumClass;
    private final E[] enumConstants;

    public EnumBasicUserType(Class<E> enumClass) {
        this.enumClass = enumClass;
        this.enumConstants = enumClass.getEnumConstants();
    }

    @Override
    public E fromString(CharSequence sequence) {
        String value = sequence.toString();
        if (Character.isDigit(value.charAt(0))) {
            return enumConstants[Integer.parseInt(value)];
        }
        return Enum.valueOf(enumClass, value);
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
