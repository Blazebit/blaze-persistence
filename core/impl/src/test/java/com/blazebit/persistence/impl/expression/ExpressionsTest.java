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

import org.junit.Test;

/**
 *
 * @author ccbem
 */
public class ExpressionsTest {
    
    public ExpressionsTest() {
    }

    @Test
    public void testParse() {
        System.out.println(Expressions.createSimpleExpression("d"));
        System.out.println(Expressions.createSimpleExpression("d.a"));
        System.out.println(Expressions.createSimpleExpression("d.a.c"));
        System.out.println(Expressions.createSimpleExpression("d[a]"));
        System.out.println(Expressions.createSimpleExpression("d.b[a]"));
        System.out.println(Expressions.createSimpleExpression("d.b[a].c"));
        System.out.println(Expressions.createSimpleExpression("d.b[a].c[e]"));
        System.out.println(Expressions.createSimpleExpression("d.b[a].c[e].f"));
        
        System.out.println(Expressions.createSimpleExpression("d[a.a]"));
        System.out.println(Expressions.createSimpleExpression("d.b[a.a]"));
        System.out.println(Expressions.createSimpleExpression("d.b[a.a].c"));
        System.out.println(Expressions.createSimpleExpression("d.b[a.a].c[e.a]"));
        System.out.println(Expressions.createSimpleExpression("d.b[a.a].c[e.a].f"));
        
        System.out.println(Expressions.createSimpleExpression("d[:a]"));
        System.out.println(Expressions.createSimpleExpression("d.b[:a]"));
        System.out.println(Expressions.createSimpleExpression("d.b[:a].c"));
        System.out.println(Expressions.createSimpleExpression("d.b[:a].c[:a]"));
        System.out.println(Expressions.createSimpleExpression("d.b[:a].c[:a].f"));
    }
    
}
