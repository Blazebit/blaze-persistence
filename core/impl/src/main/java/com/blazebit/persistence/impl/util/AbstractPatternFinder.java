/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.util;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractPatternFinder implements PatternFinder {

    @Override
    public int indexIn(CharSequence text) {
        return indexIn(text, 0, text.length());
    }

    @Override
    public int indexIn(CharSequence text, int start) {
        return indexIn(text, start, text.length());
    }

    @Override
    public int indexIn(char[] text) {
        return indexIn(text, 0, text.length);
    }

    @Override
    public int indexIn(char[] text, int start) {
        return indexIn(text, start, text.length);
    }
}
