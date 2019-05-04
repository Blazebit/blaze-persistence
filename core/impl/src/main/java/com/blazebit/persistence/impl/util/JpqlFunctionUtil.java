/*
 * Copyright 2014 - 2019 Blazebit.
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
 * @author Moritz Becker
 * @since 1.4.0
 */
public class JpqlFunctionUtil {

    private JpqlFunctionUtil() {
    }

    public static String unquote(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        boolean quote = false;
        for (int i = 1; i < s.length() - 1; i++) {
            final char c = s.charAt(i);
            if (quote) {
                quote = false;
                if (c != '\'') {
                    sb.append('\'');
                }
                sb.append(c);
            } else {
                if (c == '\'') {
                    quote = true;
                } else {
                    sb.append(c);
                }
            }
        }
        if (quote) {
            sb.append('\'');
        }
        return sb.toString();
    }
}
