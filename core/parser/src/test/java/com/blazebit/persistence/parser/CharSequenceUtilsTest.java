/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 */
public class CharSequenceUtilsTest {

    @Test
    public void testStringOnly() {
        assertEquals(11, new SubSequence("[{},{\"asd\":[{\"x\":null}]},{}]", 11, 23).lastIndexOf(']', 0, 12));
        assertEquals(1, new SubSequence("[{},{\"asd\":[{\"x\":null}]},{}]", 11, 23).indexOf('{', 1, 12));
        assertEquals(11, CharSequenceUtils.lastIndexOf(new SubSequence("[{\"f0\":\"Doc1\",\"f1\":\"0\",\"f2\":[{\"f0\":\"a\"}]},{\"f0\":\"Doc1\",\"f1\":\"0\",\"f2\":[{\"f0\":\"b\"}]},{\"f0\":\"Doc2\",\"f1\":\"0\",\"f2\":[{\"f0\":\"c\"}]}]", 110, 122), ']'));
        assertEquals(6, CharSequenceUtils.lastIndexOf(new SubSequence(new SubSequence("[{x:[{y:[{z:0}]}]}]", 4, 17), 4, 11), ']'));
    }

}
