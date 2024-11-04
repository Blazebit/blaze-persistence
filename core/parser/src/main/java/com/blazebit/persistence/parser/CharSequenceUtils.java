/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class CharSequenceUtils {

    private CharSequenceUtils() {
    }

    public static int lastIndexOf(CharSequence charSequence, char c) {
        return lastIndexOf(charSequence, c, 0, charSequence.length() - 1);
    }

    public static int lastIndexOf(CharSequence charSequence, char c, int fromIndex, int endIndex) {
        if (charSequence instanceof String) {
            int idx = ((String) charSequence).lastIndexOf(c, endIndex);
            if (idx < fromIndex) {
                return -1;
            }
            return idx;
        } else if (charSequence instanceof SubSequence) {
            int idx = ((SubSequence) charSequence).lastIndexOf(c, fromIndex, endIndex);
            if (idx == -1) {
                return -1;
            }
            return idx;
        } else {
            return lastIndexOf(charSequence.toString(), c, fromIndex, endIndex);
        }
    }

    public static int indexOf(CharSequence charSequence, char c) {
        return indexOf(charSequence, c, 0);
    }

    public static int indexOf(CharSequence charSequence, char c, int fromIndex) {
        return indexOf(charSequence, c, fromIndex, charSequence.length() - 1);
    }

    public static int indexOf(CharSequence charSequence, char c, int fromIndex, int endIndex) {
        if (charSequence instanceof String) {
            int idx = ((String) charSequence).indexOf(c, fromIndex);
            if (idx > endIndex) {
                return -1;
            }
            return idx;
        } else if (charSequence instanceof SubSequence) {
            int idx = ((SubSequence) charSequence).indexOf(c, fromIndex, endIndex);
            if (idx == -1) {
                return -1;
            }
            return idx;
        } else {
            return indexOf(charSequence.toString(), c, fromIndex, endIndex);
        }
    }

    public static int indexOf(CharSequence charSequence, String target, int fromIndex) {
        return indexOf(charSequence, target, fromIndex, charSequence.length() - 1);
    }

    public static int indexOf(CharSequence charSequence, String target, int fromIndex, int endIndex) {
        if (charSequence instanceof String) {
            int idx = ((String) charSequence).indexOf(target, fromIndex);
            if (idx > endIndex) {
                return -1;
            }
            return idx;
        } else if (charSequence instanceof SubSequence) {
            int idx = ((SubSequence) charSequence).indexOf(target, fromIndex, endIndex);
            if (idx == -1) {
                return -1;
            }
            return idx;
        } else {
            return indexOf(charSequence.toString(), target, fromIndex, endIndex);
        }
    }

}
