package com.blazebit.persistence.impl.expression;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 13.07.2016.
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
}
