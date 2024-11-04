/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.util;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface PatternFinder {

    public int indexIn(CharSequence text);

    public int indexIn(CharSequence text, int start);

    public int indexIn(CharSequence text, int start, int end);

    public int indexIn(char[] text);

    public int indexIn(char[] text, int start);

    public int indexIn(char[] text, int start, int end);

}
