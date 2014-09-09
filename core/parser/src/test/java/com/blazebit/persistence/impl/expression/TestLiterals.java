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

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Moritz Becker
 */
public class TestLiterals extends AbstractParserTest {

    @Test
    public void testEnumLiteral(){
        FooExpression result = (FooExpression) parse("ENUM(a.x.y)");
        assertEquals(new FooExpression("a.x.y"), result);
    }
    
    @Test
    public void testStringLiteral(){
        FooExpression result = (FooExpression) parse("'abcd'");
        assertEquals(new FooExpression("'abcd'"), result);
    }
    
    @Test
    public void testCharacterLiteral(){
        FooExpression result = (FooExpression) parse("'a'");
        assertEquals(new FooExpression("'a'"), result);
    }
    
    @Test
    public void testNumericLiteral(){
        FooExpression result = (FooExpression) parse("1");
        assertEquals(new FooExpression("1"), result);
    }
    
    @Test
    public void testBooleanLiteral(){
        FooExpression resultFalseUpper = (FooExpression) parse("FALSE");
        FooExpression resultFalseLower = (FooExpression) parse("false");
        FooExpression resultTrueUpper = (FooExpression) parse("TRUE");
        FooExpression resultTrueLower = (FooExpression) parse("true");
        
        assertEquals(new FooExpression("FALSE"), resultFalseUpper);
        assertEquals(new FooExpression("false"), resultFalseLower);
        assertEquals(new FooExpression("TRUE"), resultTrueUpper);
        assertEquals(new FooExpression("true"), resultTrueLower);
    }
    
    @Test
    public void testDateLiteral(){
        FooExpression result = (FooExpression) parse("(d '1991-05-21')");
        assertEquals(new FooExpression("(d '1991-05-21')"), result);
    }
    
    @Test
    public void testTimeLiteral1(){
        FooExpression result = (FooExpression) parse("(t '11:59:59.0')");
        assertEquals(new FooExpression("(t '11:59:59.0')"), result);
    }
    
    @Test
    public void testTimeLiteral2(){
        FooExpression result = (FooExpression) parse("(t '11:59:59.')");
        assertEquals(new FooExpression("(t '11:59:59.')"), result);
    }
    
    @Test
    public void testTimeLiteral3(){
        FooExpression result = (FooExpression) parse("(t '1:59:59.')");
        assertEquals(new FooExpression("(t '1:59:59.')"), result);
    }
    
    @Test
    public void testTimestampLiteral(){
        FooExpression result = (FooExpression) parse("(ts '1991-05-21 11:59:59.0')");
        assertEquals(new FooExpression("(ts '1991-05-21 11:59:59.0')"), result);
    }
}
