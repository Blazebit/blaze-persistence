/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

import java.net.MalformedURLException;
import java.net.URL;


/**
 *
 * @author Christian Beikov
 * @since 1.6.3
 */
public class URLBasicUserType extends ImmutableBasicUserType<URL> {

    public static final BasicUserType<URL> INSTANCE = new URLBasicUserType();

    @Override
    public URL fromString(CharSequence sequence) {
        try {
            return new URL(sequence.toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not deserialize url", e);
        }
    }

    @Override
    public String toStringExpression(String expression) {
        return expression;
    }
}
