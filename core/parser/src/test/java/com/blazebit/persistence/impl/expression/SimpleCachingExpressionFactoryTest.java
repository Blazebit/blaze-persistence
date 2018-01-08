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

package com.blazebit.persistence.impl.expression;

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class SimpleCachingExpressionFactoryTest {

    @Test
    public void testCreateSimpleExpressionCache() {
        ExpressionFactory ef = new SimpleCachingExpressionFactory(new ExpressionFactoryImpl(new HashSet<String>(), true, true));
        String expressionString = "SIZE(Hello.world[:hahaha].criteria[1].api.lsls[a.b.c.d.e]) + SIZE(Hello.world[:hahaha].criteria[1].api.lsls[a.b.c.d.e])";
        
        Expression expr1 = ef.createSimpleExpression(expressionString, true, null);
        Expression expr2 = ef.createSimpleExpression(expressionString, true, null);
        
        Assert.assertFalse(expr1 == expr2);
        Assert.assertEquals(expr1, expr2);
    }
}
