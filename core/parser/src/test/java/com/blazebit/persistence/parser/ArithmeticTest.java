/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
    public void testParenthesisRendering1() {
        assertEquals("1 - (1 - 1)", render(parse("1 - (1 - 1)")));
    }

    @Test
    public void testParenthesisRendering2() {
        assertEquals("1 + 1 - 1", render(parse("1 + (1 - 1)")));
    }

    @Test
    public void testParenthesisRendering3() {
        assertEquals("1 * 1 * 1", render(parse("1 * (1 * 1)")));
    }

    @Test
    public void testParenthesisRendering4() {
        assertEquals("(1 / 1) / 1", render(parse("(1 / 1) / 1")));
    }

    @Test
    public void testParenthesisRendering5() {
        assertEquals("1 * (1 - 1)", render(parse("1 * (1 - 1)")));
    }

    @Test
    public void testParenthesisRendering6() {
        assertEquals("1 * 1 - 1", render(parse("1 * 1 - 1")));
    }

    @Test
    public void testArithmeticParsing() {
        assertEquals(multiply(divide(_int("1"), _int("1")), _int("1")), parseOptimized("1 / 1 * 1"));
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
