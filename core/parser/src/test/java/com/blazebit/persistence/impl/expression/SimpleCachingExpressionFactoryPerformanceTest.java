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

import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class SimpleCachingExpressionFactoryPerformanceTest {

    private static Level originalLevel;
    
    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();
    private final ExpressionFactory cachingExpressionFactory = new SimpleCachingExpressionFactory(new ExpressionFactoryImpl(new HashSet<String>(), true));
    private final ExpressionFactory nonCachingExpressionFactory = new ExpressionFactoryImpl(new HashSet<String>(), true);
    
    @BeforeClass
    public static void beforeClass() {
        Logger log = Logger.getLogger("com.blazebit.persistence.parser");
        originalLevel = log.getLevel();
        log.setLevel(Level.OFF);
    }
    
    @AfterClass
    public static void afterClass() {
        Logger log = Logger.getLogger("com.blazebit.persistence.parser");
        log.setLevel(originalLevel);
    }

    @BenchmarkOptions(benchmarkRounds = 10000, warmupRounds = 1000, concurrency = 4)
    @Test
    public void testCreateSimpleExpressionPerformanceNonCaching() {
        testCreateSimpleExpressionPerformance(nonCachingExpressionFactory);
    }

    @BenchmarkOptions(benchmarkRounds = 10000, warmupRounds = 1000, concurrency = 4)
    @Test
    public void testCreateSimpleExpressionPerformanceCaching() {
        testCreateSimpleExpressionPerformance(cachingExpressionFactory);
    }

    private void testCreateSimpleExpressionPerformance(ExpressionFactory ef) {
        String expressionString = "SIZE(Hello.world[:hahaha].criteria[1].api.lsls[a.b.c.d.e]) + SIZE(Hello.world[:hahaha].criteria[1].api.lsls[a.b.c.d.e])";
        
        Expression expr1 = ef.createSimpleExpression(expressionString);
        Expression expr2 = ef.createSimpleExpression(expressionString);
        
        Assert.assertFalse(expr1 == expr2);
        Assert.assertEquals(expr1, expr2);
    }
}
