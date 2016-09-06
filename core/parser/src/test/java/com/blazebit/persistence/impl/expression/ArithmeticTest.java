package com.blazebit.persistence.impl.expression;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Moritz Becker
 * @since 1.2
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
