/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.util;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class QuotedIdentifierAwarePatternFinder extends AbstractPatternFinder {

    private final PatternFinder delegate;

    public QuotedIdentifierAwarePatternFinder(PatternFinder delegate) {
        this.delegate = delegate;
    }

    @Override
    public int indexIn(CharSequence text, int start, int end) {
        do {
            int index = delegate.indexIn(text, start, end);

            if (index == -1 || index == start) {
                return index;
            }

            // Check the quote modes from start to index
            QuoteMode mode = QuoteMode.NONE;
            for (int i = start; i < index; i++) {
                mode = mode.onChar(text.charAt(i));
            }

            // If the mode is none, return the index
            if (mode == QuoteMode.NONE) {
                return index;
            }

            // Otherwise increment start to the index at which the quote mode is none again
            for (start = index; start < end; start++) {
                mode = mode.onChar(text.charAt(start));
                if (mode == QuoteMode.NONE) {
                    start++;
                    break;
                }
            }
        } while (start < end);

        return -1;
    }

    @Override
    public int indexIn(char[] text, int start, int end) {
        do {
            int index = delegate.indexIn(text, start, end);

            if (index == -1 || index == start) {
                return index;
            }

            // Check the quote modes from start to index
            QuoteMode mode = QuoteMode.NONE;
            for (int i = start; i < index; i++) {
                mode = mode.onChar(text[i]);
            }

            // If the mode is none, return the index
            if (mode == QuoteMode.NONE) {
                return index;
            }

            // Otherwise increment start to the index at which the quote mode is none again
            for (start = index; start < end; start++) {
                mode = mode.onChar(text[start]);
                if (mode == QuoteMode.NONE) {
                    start++;
                    break;
                }
            }
        } while (start < end);

        return -1;
    }

}
