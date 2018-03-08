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

package com.blazebit.persistence.parser;

import com.blazebit.persistence.parser.expression.ArithmeticFactor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class ArithmeticTest extends AbstractParserTest {

    @Test
    public void testParanthesisRendering1() {
        assertEquals("1 - (1 - 1)", render(parse("1 - (1 - 1)")));
    }

    @Test
    public void testParanthesisRendering2() {
        assertEquals("1 + 1 - 1", render(parse("1 + (1 - 1)")));
    }

    @Test
    public void testParanthesisRendering3() {
        assertEquals("1 * 1 * 1", render(parse("1 * (1 * 1)")));
    }

    @Test
    public void testParanthesisRendering4() {
        assertEquals("(1 / 1) / 1", render(parse("(1 / 1) / 1")));
    }

    @Test
    public void testParanthesisRendering5() {
        assertEquals("1 * (1 - 1)", render(parse("1 * (1 - 1)")));
    }

    @Test
    public void testParanthesisRendering6() {
        assertEquals("1 * 1 - 1", render(parse("1 * 1 - 1")));
    }

    @Test
    public void testArithmeticFactorOptimization1() {
        assertEquals(_int("1"), parseOptimized("-(-1)"));
    }

    @Test
    public void testArithmeticFactorOptimization2() {
        assertEquals(new ArithmeticFactor(multiply(_int("1"), _float("3.0")), true), parseOptimized("+(-(1 * 3.0))"));
    }
}
