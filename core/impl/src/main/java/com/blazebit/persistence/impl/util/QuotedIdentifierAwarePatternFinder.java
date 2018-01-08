/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
