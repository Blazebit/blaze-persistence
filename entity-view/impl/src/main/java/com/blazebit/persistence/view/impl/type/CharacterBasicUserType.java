/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class CharacterBasicUserType extends ImmutableBasicUserType<Character> {

    public static final BasicUserType<Character> INSTANCE = new CharacterBasicUserType();

    @Override
    public Character fromString(CharSequence sequence) {
        return Character.valueOf(sequence.charAt(0));
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
