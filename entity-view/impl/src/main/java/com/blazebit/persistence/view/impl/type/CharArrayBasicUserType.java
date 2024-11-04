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
public class CharArrayBasicUserType extends AbstractMutableBasicUserType<Character[]> implements BasicUserType<Character[]> {

    public static final BasicUserType<?> INSTANCE = new CharArrayBasicUserType();

    @Override
    public boolean isDeepEqual(Character[] object1, Character[] object2) {
        return Arrays.equals(object1, object2);
    }

    @Override
    public int hashCode(Character[] object) {
        return Arrays.hashCode(object);
    }

    @Override
    public Character[] deepClone(Character[] object) {
        return Arrays.copyOf(object, object.length);
    }

    @Override
    public Character[] fromString(CharSequence sequence) {
        Character[] characters = new Character[sequence.length()];
        for (int i = 0; i < characters.length; i++) {
            characters[i] = sequence.charAt(i);
        }
        return characters;
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
