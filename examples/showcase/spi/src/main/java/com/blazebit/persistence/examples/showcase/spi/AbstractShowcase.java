/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
