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

package com.blazebit.persistence.examples.showcase.spi;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public abstract class AbstractShowcase implements Showcase {
    // helpers

    protected String heading(String heading) {
        return "\n" + frame(evenLeftRightPad(heading, 80, '-'), 80, '-') + "\n";
    }

    protected String frame(String s, int width, char frameChar) {
        final String frameLine = evenLeftRightPad("", width, frameChar);
        StringBuilder sb = new StringBuilder(frameLine).append('\n');
        if (s.endsWith("\n")) {
            sb.append(s);
        } else {
            sb.append(s).append('\n');
        }
        return sb.append(frameLine).toString();
    }

    private String evenLeftRightPad(String s, int length, char padChar) {
        length = length - s.length();
        if (length > 0) {
            s = StringUtils.leftPad(s, s.length() + length / 2 + length % 2, padChar);
            return StringUtils.rightPad(s, s.length() + length / 2, padChar);
        } else {
            return s;
        }
    }

    protected void print(Iterable<?> objects) {
        for (Object o : objects) {
            System.out.println(o);
        }
    }
}
