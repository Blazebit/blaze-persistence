/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class SubSequence implements CharSequence {
    private final CharSequence string;
    private final int start;
    private final int length;

    public SubSequence(CharSequence string, int start, int end) {
        this.string = string;
        this.start = start;
        this.length = end - start;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        if (index < 0 || index >= length) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return string.charAt(index + start);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        if (start < 0 || start >= length) {
            throw new StringIndexOutOfBoundsException(start);
        }
        if (end > length) {
            throw new StringIndexOutOfBoundsException(end);
        }
        return string.subSequence(this.start + start, this.start + end);
    }

    public int lastIndexOf(char c, int fromIndex, int endIndex) {
        int idx = CharSequenceUtils.lastIndexOf(string, c, start + fromIndex, this.start + endIndex);
        if (idx == -1) {
            return -1;
        }
        return idx - this.start;
    }

    public int indexOf(char c, int fromIndex, int endIndex) {
        int idx = CharSequenceUtils.indexOf(string, c, this.start + fromIndex, this.start + endIndex);
        if (idx == -1) {
            return -1;
        }
        return idx - this.start;
    }

    public int indexOf(String s, int fromIndex, int endIndex) {
        int idx = CharSequenceUtils.indexOf(string, s, this.start + fromIndex, this.start + endIndex);
        if (idx == -1) {
            return -1;
        }
        return idx - this.start;
    }

    @Override
    public String toString() {
        return string.subSequence(start, start + length).toString();
    }
}
