/*
 * Copyright 2014 Blazebit.
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

package com.blazebit.persistence.impl.expression;

import com.blazebit.persistence.impl.predicate.BooleanLiteral;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Moritz Becker
 */
public class TestLiterals extends AbstractParserTest {

    @Test
    public void testNullLiteral(){
        NullExpression result = (NullExpression) parse("NULL");
        assertEquals(new NullExpression(), result);
    }
    
    @Test
    public void testEnumLiteral(){
        LiteralExpression result = (LiteralExpression) parse("ENUM(a.x.y)");
        assertEquals(new LiteralExpression("ENUM", "a.x.y"), result);
    }
    
    @Test
    public void testEntityTypeLiteral(){
        LiteralExpression result = (LiteralExpression) parse("ENTITY(Entity)");
        assertEquals(new LiteralExpression("ENTITY", "Entity"), result);
    }
    
    @Test
    public void testStringLiteral(){
        StringLiteral result = (StringLiteral) parse("'abcd'");
        assertEquals(new StringLiteral("abcd"), result);
    }
    
    @Test
    public void testCharacterLiteral(){
        StringLiteral result = (StringLiteral) parse("'a'");
        assertEquals(new StringLiteral("a"), result);
    }
    
    @Test
    public void testIntegerLiteral(){
        assertEquals(new NumericLiteral("1", NumericType.INTEGER), parse("1"));
    }

    @Test(expected = SyntaxErrorException.class)
    public void testIntegerLeadingZero(){
        parse("01");
    }

    @Test
    public void testFloatLiteral(){
        assertEquals(new NumericLiteral("1F", NumericType.FLOAT), parse("1F"));
    }

    @Test
    public void testFloatLiteralWithDecimal(){
        assertEquals(new NumericLiteral("121.223F", NumericType.FLOAT), parse("121.223F"));
    }

    @Test
    public void testFloatLiteralWithDecimalAndExp(){
        assertEquals(new NumericLiteral("121.223e21F", NumericType.FLOAT), parse("121.223e21F"));
    }

    @Test
    public void testFloatLiteralWithExp(){
        assertEquals(new NumericLiteral("121.e21F", NumericType.FLOAT), parse("121.e21F"));
    }

    @Test
    public void testDotFloatLiteral(){
        assertEquals(new NumericLiteral(".23", NumericType.FLOAT), parse(".23"));
    }

    @Test
    public void testDotFloatLiteralWithDecimalAndNegativeExp(){
        assertEquals(new NumericLiteral(".23e-21F", NumericType.FLOAT), parse(".23e-21F"));
    }

    @Test
    public void testDotFloatLiteralWithDecimalAndExp(){
        assertEquals(new NumericLiteral(".23e21F", NumericType.FLOAT), parse(".23e21F"));
    }

    @Test
    public void testDoubleLiteral(){
        assertEquals(new NumericLiteral("1D", NumericType.DOUBLE), parse("1D"));
    }

    @Test
    public void testDoubleLiteralWithDecimal(){
        assertEquals(new NumericLiteral("121.223D", NumericType.DOUBLE), parse("121.223D"));
    }

    @Test
    public void testDoubleLiteralWithDecimalAndExp(){
        assertEquals(new NumericLiteral("121.223e21D", NumericType.DOUBLE), parse("121.223e21D"));
    }

    @Test
    public void testDotDoubleLiteral(){
        assertEquals(new NumericLiteral(".23D", NumericType.DOUBLE), parse(".23D"));
    }

    @Test
    public void testDotDoubleLiteralWithDecimalAndNegativeExp(){
        assertEquals(new NumericLiteral(".23e-21D", NumericType.DOUBLE), parse(".23e-21D"));
    }

    @Test
    public void testDotDoubleLiteralWithDecimalAndExp(){
        assertEquals(new NumericLiteral(".23e21D", NumericType.DOUBLE), parse(".23e21D"));
    }

    @Test
    public void testBigDecimalLiteral(){
        assertEquals(new NumericLiteral("1BD", NumericType.BIG_DECIMAL), parse("1BD"));
    }

    @Test
    public void testBigDecimalLiteralWithDecimal(){
        assertEquals(new NumericLiteral("121.223BD", NumericType.BIG_DECIMAL), parse("121.223BD"));
    }

    @Test
    public void testBigDecimalLiteralWithDecimalAndExp(){
        assertEquals(new NumericLiteral("121.223e21BD", NumericType.BIG_DECIMAL), parse("121.223e21BD"));
    }

    @Test
    public void testDotBigDecimalLiteral(){
        assertEquals(new NumericLiteral(".23BD", NumericType.BIG_DECIMAL), parse(".23BD"));
    }

    @Test
    public void testDotBigDecimalLiteralWithDecimalAndNegativeExp(){
        assertEquals(new NumericLiteral(".23e-21BD", NumericType.BIG_DECIMAL), parse(".23e-21BD"));
    }

    @Test
    public void testDotBigDecimalLiteralWithDecimalAndExp(){
        assertEquals(new NumericLiteral(".23e21BD", NumericType.BIG_DECIMAL), parse(".23e21BD"));
    }

    @Test
    public void testLongLiteral(){
        assertEquals(new NumericLiteral("1L", NumericType.LONG), parse("1L"));
    }

    @Test(expected = SyntaxErrorException.class)
    public void testLongLeadingZero(){
        parse("01L");
    }

    @Test
    public void testBigIntegerLiteral(){
        assertEquals(new NumericLiteral("1BI", NumericType.BIG_INTEGER), parse("1BI"));
    }

    @Test(expected = SyntaxErrorException.class)
    public void testBigIntegerLeadingZero(){
        parse("01BI");
    }
    
    @Test
    public void testBooleanLiteral(){
        BooleanLiteral resultFalseUpper = (BooleanLiteral) parse("FALSE");
        BooleanLiteral resultFalseLower = (BooleanLiteral) parse("false");
        BooleanLiteral resultFalseMixed = (BooleanLiteral) parse("FaLsE");
        BooleanLiteral resultTrueUpper = (BooleanLiteral) parse("TRUE");
        BooleanLiteral resultTrueLower = (BooleanLiteral) parse("true");
        BooleanLiteral resultTrueMixed = (BooleanLiteral) parse("TrUe");

        assertEquals(_boolean(false), resultFalseUpper);
        assertEquals(_boolean(false), resultFalseLower);
        assertEquals(_boolean(false), resultFalseMixed);
        assertEquals(_boolean(true), resultTrueUpper);
        assertEquals(_boolean(true), resultTrueLower);
        assertEquals(_boolean(true), resultTrueMixed);
    }
    
    @Test
    public void testDateLiteral(){
        DateLiteral result = (DateLiteral) parse("{d '1991-05-21'}");
        assertEquals(_date(1991, 5, 21), result);
    }
    
    @Test
    public void testTimeLiteral1(){
        TimeLiteral result = (TimeLiteral) parse("{t '11:59:59.100'}");
        assertEquals(_time(11, 59, 59, 100), result);
    }
    
    @Test
    public void testTimeLiteral2(){
        TimeLiteral result = (TimeLiteral) parse("{t '11:59:59.'}");
        assertEquals(_time(11, 59, 59, 0), result);
    }
    
    @Test
    public void testTimeLiteral3(){
        TimeLiteral result = (TimeLiteral) parse("{t '1:59:59.'}");
        assertEquals(_time(1, 59, 59, 0), result);
    }
    
    @Test
    public void testTimestampLiteral(){
        TimestampLiteral result = (TimestampLiteral) parse("{ts '1991-05-21 11:59:59.100'}");
        assertEquals(_timestamp(1991, 5, 21, 11, 59, 59, 100), result);
    }

}
