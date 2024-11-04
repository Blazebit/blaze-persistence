/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a specialized parser for the JSON structure that will be produced by the JSON functions in Blaze-Persistence.
 * The parser can only parse valid JSON that conforms to that structure. Other JSON may run into problems.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class JsonParser {

    private JsonParser() {
    }

    public static List<Object[]> parseStringOnly(CharSequence json, String... fields) {
        List<Object[]> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        if (json != null && json.length() != 0) {
            Map<String, Integer> fieldMap = new HashMap<>(fields.length);
            for (int i = 0; i < fields.length; i++) {
                fieldMap.put(fields[i], i);
            }

            int start = CharSequenceUtils.indexOf(json, '[') + 1;
            int end = CharSequenceUtils.lastIndexOf(json, ']');
            for (int i = start; i < end; i++) {
                Object[] object = new Object[fields.length];
                boolean quoteMode = false;
                int fieldIndex = -1;
                boolean escapes = false;
                i = CharSequenceUtils.indexOf(json, '{', i) + 1;
                for (; i < end; i++) {
                    char c = json.charAt(i);
                    if (!quoteMode) {
                        if (c == '"') {
                            quoteMode = true;
                        } else if (c == '}') {
                            break;
                        } else if (c != ':' && c != ',' && !Character.isWhitespace(c)) {
                            // non-string value
                            switch (c) {
                                case 'n':
                                    object[fieldIndex] = null;
                                    i += 3;
                                    fieldIndex = -1;
                                    escapes = false;
                                    break;
                                case '[':
                                    // Nested object handling
                                    int nestedEnd = findEnd(json, i);
                                    object[fieldIndex] = new SubSequence(json, i, nestedEnd);
                                    fieldIndex = -1;
                                    i = nestedEnd - 1;
                                    break;
                                default:
                                    throw new IllegalArgumentException("Non-String value unsupported! Found at: " + i);
                            }
                        }
                    } else {
                        if (c == '\\') {
                            escapes = true;
                            c = json.charAt(++i);
                            switch (c) {
                                case 'b':
                                    c = '\b';
                                    break;
                                case 'f':
                                    c = '\f';
                                    break;
                                case 'r':
                                    c = '\r';
                                    break;
                                case 'n':
                                    c = '\n';
                                    break;
                                case 't':
                                    c = '\t';
                                    break;
                                case 'u':
                                    c = (char) Integer.parseInt(json.subSequence(i + 1, i + 5).toString(), 16);
                                    i += 4;
                                    break;
                                case '"':
                                case '\\':
                                case '/':
                                    break;
                                default:
                                    throw new IllegalStateException("Unexpected escape sequence at position: " + i);
                            }
                            sb.append(c);
                        } else if (c == '"') {
                            if (fieldIndex == -1) {
                                fieldIndex = fieldMap.get(sb.toString());
                            } else {
                                if (escapes) {
                                    object[fieldIndex] = sb.toString();
                                } else {
                                    object[fieldIndex] = new SubSequence(json, i - sb.length(), i);
                                }
                                fieldIndex = -1;
                                escapes = false;
                            }
                            sb.setLength(0);
                            quoteMode = false;
                        } else {
                            sb.append(c);
                        }
                    }
                }
                list.add(object);
            }
        }
        return list;
    }

    private static int findEnd(CharSequence json, int i) {
        int arrayLevel = 1;
        int end = json.length();
        boolean quoteMode = false;
        i++;
        for (;i < end; i++) {
            final char c = json.charAt(i);
            if (!quoteMode) {
                switch (c) {
                    case '"':
                        quoteMode = true;
                        break;
                    case '[':
                        arrayLevel++;
                        break;
                    case ']':
                        arrayLevel--;
                        if (arrayLevel == 0) {
                            return i + 1;
                        }
                        break;
                    default:
                        break;
                }
            } else if (c == '\\') {
                i++;
            } else if (c == '"') {
                quoteMode = false;
            }
        }
        return i;
    }

}
