/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.AbstractMutableBasicUserType;
import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.util.Arrays;
import java.util.Base64;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PrimitiveByteArrayBasicUserType extends AbstractMutableBasicUserType<byte[]> implements BasicUserType<byte[]> {

    public static final BasicUserType<?> INSTANCE = new PrimitiveByteArrayBasicUserType();

    @Override
    public boolean isDeepEqual(byte[] object1, byte[] object2) {
        return Arrays.equals(object1, object2);
    }

    @Override
    public int hashCode(byte[] object) {
        return Arrays.hashCode(object);
    }

    @Override
    public byte[] deepClone(byte[] object) {
        return Arrays.copyOf(object, object.length);
    }

    @Override
    public byte[] fromString(CharSequence sequence) {
        return Base64.getDecoder().decode(sequence.toString());
    }

    @Override
    public String toStringExpression(String expression) {
        return "BASE64(" + expression + ")";
    }
}
