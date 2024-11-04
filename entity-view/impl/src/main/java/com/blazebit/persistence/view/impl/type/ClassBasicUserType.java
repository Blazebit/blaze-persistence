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
 * @since 1.6.3
 */
public class ClassBasicUserType extends ImmutableBasicUserType<Class<?>> {

    public static final BasicUserType<Class<?>> INSTANCE = new ClassBasicUserType();

    @Override
    public Class<?> fromString(CharSequence sequence) {
        try {
            return Class.forName(sequence.toString());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not deserialize class", e);
        }
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
