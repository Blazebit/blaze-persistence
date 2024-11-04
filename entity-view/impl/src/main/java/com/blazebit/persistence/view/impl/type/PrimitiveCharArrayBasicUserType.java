/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.AbstractMutableBasicUserType;
import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.util.Arrays;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PrimitiveCharArrayBasicUserType extends AbstractMutableBasicUserType<char[]> implements BasicUserType<char[]> {

    public static final BasicUserType<?> INSTANCE = new PrimitiveCharArrayBasicUserType();

    @Override
    public boolean isDeepEqual(char[] object1, char[] object2) {
        return Arrays.equals(object1, object2);
    }

    @Override
    public int hashCode(char[] object) {
        return Arrays.hashCode(object);
    }

    @Override
    public char[] deepClone(char[] object) {
        return Arrays.copyOf(object, object.length);
    }

    @Override
    public char[] fromString(CharSequence sequence) {
        char[] chars = new char[sequence.length()];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = sequence.charAt(i);
        }
        return chars;
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
