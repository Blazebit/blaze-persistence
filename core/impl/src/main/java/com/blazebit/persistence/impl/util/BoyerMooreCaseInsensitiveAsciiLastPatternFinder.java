package com.blazebit.persistence.impl.util;

public class BoyerMooreCaseInsensitiveAsciiLastPatternFinder extends AbstractPatternFinder {

    // Only support ASCII
    private static final int RADIX = 256;
    private final int[] left;
    private final char[] pattern;

    public BoyerMooreCaseInsensitiveAsciiLastPatternFinder(String pattern) {
        final int length = pattern.length();
        this.pattern = new char[length];

        this.left = new int[RADIX];
        for (int i = 0; i < RADIX; i++) {
            this.left[i] = length;
        }
        for (int i = 0; i < length; i++) {
            final char c = Character.toLowerCase(pattern.charAt(i));
            this.pattern[i] = c;
            this.left[c] = length - i;
        }
    }

    public int indexIn(char[] text, int start, int end) {
        int m = pattern.length;
        int skip;
        for (int i = end - m; i >= start; i -= skip) {
            skip = 0;
            for (int j = 0; j < m; j++) {
                final char c = Character.toLowerCase(text[i + j]);
                if (pattern[j] != c) {
                    skip = Math.max(1, left[c] - j);
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
        int skip;
        for (int i = end - m; i >= start; i -= skip) {
            skip = 0;
            for (int j = 0; j < m; j++) {
                final char c = Character.toLowerCase(text.charAt(i + j));
                if (pattern[j] != c) {
                    skip = Math.max(1, left[c] - j);
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
