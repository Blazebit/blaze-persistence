/*
 * Copyright 2014 - 2023 Blazebit.
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
