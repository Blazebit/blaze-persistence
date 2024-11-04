/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.impl.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class JpqlFunctionUtilTest {

    @Test
    public void testUnquoteSingleQuotes() {
        assertEquals("0", JpqlFunctionUtil.unquoteSingleQuotes("0"));
        assertEquals("0", JpqlFunctionUtil.unquoteSingleQuotes("'0'"));
        assertEquals("'0'", JpqlFunctionUtil.unquoteSingleQuotes("''0''"));
    }

    @Test
    public void testUnquoteDoubleQuotes() {
        assertEquals("0", JpqlFunctionUtil.unquoteDoubleQuotes("0"));
        assertEquals("0", JpqlFunctionUtil.unquoteDoubleQuotes("\"0\""));
        assertEquals("\"0\"", JpqlFunctionUtil.unquoteDoubleQuotes("\"\"0\"\""));
    }
}
