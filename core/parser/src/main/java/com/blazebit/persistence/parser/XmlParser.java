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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a specialized parser for the XML structure that will be produced by the XML functions in Blaze-Persistence.
 * The parser can only parse valid XML that conforms to that structure. Other XML may run into problems.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class XmlParser {

    private XmlParser() {
    }

    public static List<Object[]> parse(CharSequence xml, String... fields) {
        List<Object[]> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        if (xml != null && xml.length() != 0) {
            Map<String, Integer> fieldMap = new HashMap<>(fields.length);
            for (int i = 0; i < fields.length; i++) {
                fieldMap.put(fields[i], i);
            }

            for (int i = 0; i < xml.length(); i++) {
                Object[] object = new Object[fields.length];
                boolean key = true;
                boolean escape = false;
                int fieldIndex = -1;
                i = CharSequenceUtils.indexOf(xml, "<e>", i) + 3;
                LOOP: for (; i < xml.length(); i++) {
                    char c = xml.charAt(i);
                    switch (c) {
                        case '<':
                            int endIndex = CharSequenceUtils.indexOf(xml, '>', i);
                            if (key) {
                                if (xml.charAt(i + 1) == '/') {
                                    i = endIndex;
                                    break LOOP;
                                }
                                String tag = xml.subSequence(i + 1, endIndex).toString();
                                fieldIndex = fieldMap.get(tag);
                                i = endIndex;
                                key = false;
                            } else {
                                String tag = xml.subSequence(i + 2, endIndex).toString();
                                if (xml.charAt(i + 1) != '/' || !tag.equals(fields[fieldIndex])) {
                                    throw new IllegalStateException("Unexpected tag at position: " + i);
                                }
                                if (escape) {
                                    object[fieldIndex] = sb.toString();
                                } else {
                                    object[fieldIndex] = new SubSequence(xml, i - sb.length(), i);
                                }
                                key = true;
                                fieldIndex = -1;
                                i = endIndex;
                            }
                            sb.setLength(0);
                            break;
                        case '&':
                            escape = true;
                            switch (xml.charAt(i + 1)) {
                                case 'a':
                                    sb.append('&');
                                    i += 4;
                                    break;
                                case 'l':
                                    sb.append('<');
                                    i += 3;
                                    break;
                                case 'g':
                                    sb.append('>');
                                    i += 3;
                                    break;
                                default:
                                    throw new IllegalStateException("Unexpected escape sequence at position: " + i);
                            }
                            break;
                        default:
                            sb.append(c);
                            break;
                    }
                }
                list.add(object);
            }
        }
        return list;
    }
}
