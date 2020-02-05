/*
 * Copyright 2014 - 2020 Blazebit.
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
