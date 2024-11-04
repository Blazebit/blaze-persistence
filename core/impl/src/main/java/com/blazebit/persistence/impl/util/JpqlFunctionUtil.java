/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.util;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
public class JpqlFunctionUtil {

    private static final char SINGLE_QUOTE_CHARACTER = '\'';

    private JpqlFunctionUtil() {
    }

    public static String unquoteSingleQuotes(String s) {
        return unquote(s, SINGLE_QUOTE_CHARACTER);
    }

    public static String unquoteDoubleQuotes(String s) {
        return unquote(s, '\"');
    }

    private static String unquote(String s, char quoteCharacter) {
        if (!s.isEmpty() && s.charAt(0) != quoteCharacter) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s.length());
        boolean quote = false;
        for (int i = 1; i < s.length() - 1; i++) {
            final char c = s.charAt(i);
            if (quote) {
                quote = false;
                if (c != quoteCharacter) {
                    sb.append(quoteCharacter);
                }
                sb.append(c);
            } else {
                if (c == quoteCharacter) {
                    quote = true;
                } else {
                    sb.append(c);
                }
            }
        }
        if (quote) {
            sb.append(quoteCharacter);
        }
        return sb.toString();
    }

    public static String quoteSingle(String s) {
        return SINGLE_QUOTE_CHARACTER + s + SINGLE_QUOTE_CHARACTER;
    }
}
