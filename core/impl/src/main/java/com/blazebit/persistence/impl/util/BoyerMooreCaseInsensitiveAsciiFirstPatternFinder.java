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
public class BoyerMooreCaseInsensitiveAsciiFirstPatternFinder extends AbstractPatternFinder {

    // Only support ASCII
    private static final int RADIX = 256;
    private final int[] right;
    private final char[] pattern;

    public BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(String pattern) {
        final int length = pattern.length();
        this.pattern = new char[length];

        this.right = new int[RADIX];
        for (int i = 0; i < RADIX; i++) {
            this.right[i] = -1;
        }
        for (int i = 0; i < length; i++) {
            final char c = Character.toLowerCase(pattern.charAt(i));
            this.pattern[i] = c;
            this.right[c] = i;
        }
    }

    public int indexIn(char[] text, int start, int end) {
        int m = pattern.length;
        int n = Math.min(text.length, end);
        int skip;
        for (int i = start; i <= n - m; i += skip) {
            skip = 0;
            for (int j = m - 1; j >= 0; j--) {
                final char c = Character.toLowerCase(text[i + j]);
                if (pattern[j] != c) {
                    skip = Math.max(1, j - right[c]);
                    break;
                }
            }
            if (skip == 0) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int indexIn(CharSequence text, int start, int end) {
        int m = pattern.length;
        int n = Math.min(text.length(), end);
        int skip;
        for (int i = start; i <= n - m; i += skip) {
            skip = 0;
            for (int j = m - 1; j >= 0; j--) {
                final char c = Character.toLowerCase(text.charAt(i + j));
                if (pattern[j] != c) {
                    skip = Math.max(1, j - right[c]);
                    break;
                }
            }
            if (skip == 0) {
                return i;
            }
        }
        return -1;
    }
}
